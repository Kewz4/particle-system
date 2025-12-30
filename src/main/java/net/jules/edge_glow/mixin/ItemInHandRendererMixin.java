package net.jules.edge_glow.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jules.edge_glow.EdgeGlowMod;
import net.jules.edge_glow.config.ConfigLoader;
import net.jules.edge_glow.config.GlowConfig;
import net.jules.edge_glow.logic.EdgeDetector;
import net.jules.edge_glow.logic.PointCache;
import net.jules.edge_glow.particle.GlowParticleOptions;
import net.jules.edge_glow.init.ModParticles;
import net.jules.edge_glow.utils.TexturePixelReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    // Rate limit logs
    private static int logCounter = 0;

    @Inject(method = "renderItem", at = @At("HEAD"))
    public void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
        if (Minecraft.getInstance().isPaused()) return;
        if (stack.isEmpty()) return;

        // DEBUG: Log context occasionally
        if (logCounter++ % 1000 == 0) {
             // EdgeGlowMod.LOGGER.info("Rendering item: {} in context {}", stack.getItem(), displayContext);
        }

        GlowConfig config = ConfigLoader.ITEM_RULES.get(stack.getItem());
        if (config == null) return;

        if (config.name_pattern != null && !config.name_pattern.isEmpty()) {
            String displayName = stack.getHoverName().getString();
            if (!displayName.matches(config.name_pattern)) {
                 return;
            }
        }

        // DEBUG: Match found
        if (logCounter % 200 == 0) {
             EdgeGlowMod.LOGGER.info("EdgeGlow: Triggered for {}, Hand: {}", stack.getHoverName().getString(), displayContext);
        }

        if (displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && displayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return;
        }

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level(), entity, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();
        if (sprite == null) return;

        ResourceLocation spriteId = sprite.contents().name();

        List<Vector2f> edges;
        if (PointCache.has(spriteId)) {
            edges = PointCache.get(spriteId);
        } else {
            var nativeImageOpt = TexturePixelReader.getImageFromSprite(sprite);
            if (nativeImageOpt.isPresent()) {
                edges = EdgeDetector.detectEdges(nativeImageOpt.get(), config.alpha_threshold, 1);
                PointCache.put(spriteId, edges);
                EdgeGlowMod.LOGGER.info("EdgeGlow: Detected {} edges for {}", edges.size(), spriteId);
            } else {
                PointCache.put(spriteId, List.of());
                EdgeGlowMod.LOGGER.warn("EdgeGlow: Could not read pixels for {}", spriteId);
                return;
            }
        }

        if (edges.isEmpty()) return;

        int count = (int) (edges.size() * config.spawn_rate);
        if (count <= 0 && config.spawn_rate > 0 && Math.random() < config.spawn_rate) count = 1;

        if (count > 0) {
            poseStack.pushPose();
            model.getTransforms().getTransform(displayContext).apply(leftHand, poseStack);

            Matrix4f pose = poseStack.last().pose();
            Vector3f cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

            poseStack.popPose();

            net.minecraft.core.particles.ParticleOptions particleOptions = null;

            if (config.particle.equals(ModParticles.GLOW.getId())) {
                Vector3f start = parseColor(config.gradient_start);
                Vector3f end = parseColor(config.gradient_end);
                particleOptions = new GlowParticleOptions(start, end);
            } else if (ForgeRegistries.PARTICLE_TYPES.containsKey(config.particle)) {
                 particleOptions = (net.minecraft.core.particles.ParticleOptions) ForgeRegistries.PARTICLE_TYPES.getValue(config.particle);
            }

            if (particleOptions == null) return;

            for (int i = 0; i < count; i++) {
                Vector2f edge = edges.get((int) (Math.random() * edges.size()));

                float lx = edge.x;
                float ly = 1.0f - edge.y; // UV Flip
                float lz = 0.5f + (float)(Math.random() * 0.06 - 0.03);

                Vector4f vec = new Vector4f(lx, ly, lz, 1.0f);
                pose.transform(vec);

                double wx = vec.x + cameraPos.x;
                double wy = vec.y + cameraPos.y;
                double wz = vec.z + cameraPos.z;

                double vx = (Math.random() - 0.5) * 0.005;
                double vy = (Math.random() - 0.5) * 0.005;
                double vz = (Math.random() - 0.5) * 0.005;

                entity.level().addParticle(particleOptions, wx, wy, wz, vx, vy, vz);
            }
        }
    }

    private Vector3f parseColor(String hex) {
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            int color = Integer.parseInt(hex, 16);
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            return new Vector3f(r, g, b);
        } catch (Exception e) {
            return new Vector3f(1, 1, 1);
        }
    }
}
