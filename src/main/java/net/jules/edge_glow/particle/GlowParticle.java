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

    protected GlowParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, GlowParticleOptions options) {
        super(level, x, y, z, vx, vy, vz);
        this.friction = 0.96F;
        this.gravity = 0;
        this.quadSize = 0.1F; // small
        this.lifetime = 20 + this.random.nextInt(10);

        this.startColor = options.getStartColor();
        this.endColor = options.getEndColor();

        this.rCol = startColor.x;
        this.gCol = startColor.y;
        this.bCol = startColor.z;
        this.alpha = 1.0f;

        this.xd = vx * 0.05;
        this.yd = vy * 0.05;
        this.zd = vz * 0.05;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
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

        float scale = this.getQuadSize(partialTicks);
        Quaternionf quaternion = camera.rotation();

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = corners[i];
            vector3f.rotate(quaternion);
            vector3f.mul(scale);
            vector3f.add((float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x),
                         (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y),
                         (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z));
        }

        // Procedural Glow: Center opaque, edges transparent
        // We draw 4 triangles meeting at center?
        // Or just a quad where vertices have 0 alpha?
        // If we draw a quad with all vertices having Alpha 0, it is invisible.
        // We need a center point.
        // Let's draw 4 triangles (Fan)

        Vector3f center = new Vector3f(0,0,0);
        center.add((float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x),
                   (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y),
                   (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z));

        // Draw 4 triangles: Center -> Corner 1 -> Corner 2

        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        float a = this.alpha;

        // Triangle 1: Center, 0, 1
        vertex(buffer, center, r, g, b, a);
        vertex(buffer, corners[0], r, g, b, 0); // Edge alpha 0
        vertex(buffer, corners[1], r, g, b, 0);

        // Triangle 2: Center, 1, 2
        vertex(buffer, center, r, g, b, a);
        vertex(buffer, corners[1], r, g, b, 0);
        vertex(buffer, corners[2], r, g, b, 0);

        // Triangle 3: Center, 2, 3
        vertex(buffer, center, r, g, b, a);
        vertex(buffer, corners[2], r, g, b, 0);
        vertex(buffer, corners[3], r, g, b, 0);

        // Triangle 4: Center, 3, 0
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
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        @Override
        public String toString() { return "PROCEDURAL_GLOW"; }
    };

    public static class Provider implements ParticleProvider<GlowParticleOptions> {
        public Provider(Object unused) {} // No SpriteSet needed

        @Override
        public Particle createParticle(GlowParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new GlowParticle(level, x, y, z, dx, dy, dz, options);
        }
    }
}
