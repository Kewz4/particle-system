package net.jules.edge_glow.init;

import com.mojang.serialization.Codec;
import net.jules.edge_glow.EdgeGlowMod;
import net.jules.edge_glow.particle.GlowParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, EdgeGlowMod.MODID);

    public static final RegistryObject<ParticleType<GlowParticleOptions>> GLOW = PARTICLES.register("glow",
            () -> new ParticleType<GlowParticleOptions>(false, GlowParticleOptions.DESERIALIZER) {
                @Override
                public Codec<GlowParticleOptions> codec() {
                    return GlowParticleOptions.CODEC;
                }
            });

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
