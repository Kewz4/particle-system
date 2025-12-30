package net.jules.edge_glow.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jules.edge_glow.config.ConfigLoader;
import net.jules.edge_glow.config.GlowConfig;
import net.jules.edge_glow.logic.EdgeDetector;
import net.jules.edge_glow.logic.PointCache;
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

    @Inject(method = "renderItem", at = @At("HEAD"))
    public void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
        if (Minecraft.getInstance().isPaused()) return;
        if (stack.isEmpty()) return;

        // Check config
        GlowConfig config = ConfigLoader.ITEM_RULES.get(stack.getItem());
        if (config == null) return;

        // Check Display Context (First Person Only as requested)
        if (displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && displayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return;
        }

        // Retrieve Model and Sprite
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level(), entity, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();
        if (sprite == null) return;

        ResourceLocation spriteId = sprite.contents().name();

        // Get or Compute Edges
        List<Vector2f> edges;
        if (PointCache.has(spriteId)) {
            edges = PointCache.get(spriteId);
        } else {
            // Compute
            var nativeImageOpt = TexturePixelReader.getImageFromSprite(sprite);
            if (nativeImageOpt.isPresent()) {
                edges = EdgeDetector.detectEdges(nativeImageOpt.get(), config.alpha_threshold, 1);
                PointCache.put(spriteId, edges);
            } else {
                PointCache.put(spriteId, List.of());
                return;
            }
        }

        if (edges.isEmpty()) return;

        int count = (int) (edges.size() * config.spawn_rate);
        if (count <= 0 && config.spawn_rate > 0 && Math.random() < config.spawn_rate) count = 1;

        if (count > 0) {
            // Push pose to apply item transforms
            poseStack.pushPose();
            // Apply the item's display transform (e.g. rotation, scale in hand)
            model.getTransforms().getTransform(displayContext).apply(leftHand, poseStack);

            // Apply translations to center the item model if needed
            // Standard items render centered at 0.5, 0.5, 0.5 inside the block/item renderer,
            // but here we are in the hand renderer.
            // The item transform assumes the origin is the "handle".
            // We'll proceed with the assumption that our 0-1 UVs need to be mapped to -0.5 to 0.5 or 0 to 1 depending on how the model is built.
            // Usually, generated item models (2D) are centered.
            // Let's assume standard behavior: 0.5, 0.5, 0.5 is center.

            Matrix4f pose = poseStack.last().pose();
            Vector3f cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

            poseStack.popPose(); // We have the matrix, we can pop now.

            for (int i = 0; i < count; i++) {
                Vector2f edge = edges.get((int) (Math.random() * edges.size()));

                // Map UV to 3D.
                // In generated models, the texture is on the Z-plane.
                // We need to map UV (0..1) to X/Y coordinates.
                // Standard mapping: X = u, Y = 1-v (or v inverted).
                // Centering: usually items are rendered such that (0.5, 0.5) is the pivot?
                // Actually, 'Generated' models usually extend from 0 to 1 in X and Y.
                // But the 'GUI' transform or 'First Person' transform scales/translates it.
                // Let's assume 0..1 range first.

                float lx = edge.x;
                float ly = 1.0f - edge.y;
                float lz = 0.5f + (float)(Math.random() * 0.06 - 0.03);

                Vector4f vec = new Vector4f(lx, ly, lz, 1.0f);
                vec.mul(pose);

                double wx = vec.x + cameraPos.x;
                double wy = vec.y + cameraPos.y;
                double wz = vec.z + cameraPos.z;

                double vx = (Math.random() - 0.5) * 0.01;
                double vy = (Math.random() - 0.5) * 0.01;
                double vz = (Math.random() - 0.5) * 0.01;

                if (ForgeRegistries.PARTICLE_TYPES.containsKey(config.particle)) {
                     entity.level().addParticle(
                         (net.minecraft.core.particles.ParticleOptions) ForgeRegistries.PARTICLE_TYPES.getValue(config.particle),
                         wx, wy, wz, vx, vy, vz
                     );
                }
            }
        }
    }
}
