package net.jules.edge_glow.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class GlowParticle extends TextureSheetParticle {

    protected GlowParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.quadSize *= 0.75F;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);

        // Low velocity for aura effect
        this.xd = vx * 0.05;
        this.yd = vy * 0.05;
        this.zd = vz * 0.05;

        this.lifetime = 20 + this.random.nextInt(10);
        this.setAlpha(0.8f);
    }

    @Override
    public void tick() {
        super.tick();
        this.setAlpha(0.8f * (1.0f - ((float)this.age / this.lifetime)));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new GlowParticle(level, x, y, z, dx, dy, dz, this.sprites);
        }
    }
}
