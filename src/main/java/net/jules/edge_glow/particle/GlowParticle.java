package net.jules.edge_glow.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GlowParticle extends Particle {

    private final Vector3f startColor;
    private final Vector3f endColor;
    private final float baseSize;

    protected GlowParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, GlowParticleOptions options) {
        super(level, x, y, z, vx, vy, vz);
        this.friction = 0.96F;
        this.gravity = 0;
        this.baseSize = 0.2F;
        this.lifetime = 20 + this.random.nextInt(10);

        this.startColor = options.getStartColor();
        this.endColor = options.getEndColor();

        this.rCol = startColor.x;
        this.gCol = startColor.y;
        this.bCol = startColor.z;
        this.alpha = 1.0f;

        // Initial velocity from mixin
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // Apply Physics: Sine Wave Motion (simulating the example snippet)
            // particle.dx += 0.005F * MathHelper.sin(0.3F * player.age);
            float time = this.age * 0.3F;
            this.xd += 0.002F * Mth.sin(time);
            this.zd += 0.002F * Mth.cos(time);
            this.yd += 0.001F; // Slight rise

            // Interpolate color
            float progress = (float)this.age / (float)this.lifetime;
            this.rCol = Mth.lerp(progress, startColor.x, endColor.x);
            this.gCol = Mth.lerp(progress, startColor.y, endColor.y);
            this.bCol = Mth.lerp(progress, startColor.z, endColor.z);

            // Fade alpha
            this.alpha = 1.0f - progress;

            this.move(this.xd, this.yd, this.zd);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vector3f[] corners = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, -1.0F, 0.0F)
        };

        float scale = this.baseSize;
        Quaternionf quaternion = camera.rotation();

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = corners[i];
            vector3f.rotate(quaternion);
            vector3f.mul(scale);
            vector3f.add((float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x),
                         (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y),
                         (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z));
        }

        Vector3f center = new Vector3f(0,0,0);
        center.add((float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x),
                   (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y),
                   (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z));

        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        float a = this.alpha;

        // Triangle Fan
        vertex(buffer, center, r, g, b, a);
        vertex(buffer, corners[0], r, g, b, 0);
        vertex(buffer, corners[1], r, g, b, 0);

        vertex(buffer, center, r, g, b, a);
        vertex(buffer, corners[1], r, g, b, 0);
        vertex(buffer, corners[2], r, g, b, 0);

        vertex(buffer, center, r, g, b, a);
        vertex(buffer, corners[2], r, g, b, 0);
        vertex(buffer, corners[3], r, g, b, 0);

        vertex(buffer, center, r, g, b, a);
        vertex(buffer, corners[3], r, g, b, 0);
        vertex(buffer, corners[0], r, g, b, 0);
    }

    private void vertex(VertexConsumer buffer, Vector3f pos, float r, float g, float b, float a) {
        buffer.vertex(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return PROCEDURAL_GLOW;
    }

    public static final ParticleRenderType PROCEDURAL_GLOW = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager manager) {
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA, com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
        }

        @Override
        public String toString() { return "PROCEDURAL_GLOW"; }
    };

    public static class Provider implements ParticleProvider<GlowParticleOptions> {
        public Provider(Object unused) {}

        @Override
        public Particle createParticle(GlowParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new GlowParticle(level, x, y, z, dx, dy, dz, options);
        }
    }
}
