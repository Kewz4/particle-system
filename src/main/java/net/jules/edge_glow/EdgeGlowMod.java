package net.jules.edge_glow;

import com.mojang.logging.LogUtils;
import net.jules.edge_glow.config.ConfigLoader;
import net.jules.edge_glow.init.ModParticles;
import net.jules.edge_glow.particle.GlowParticle;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EdgeGlowMod.MODID)
public class EdgeGlowMod {
    public static final String MODID = "edge_glow";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EdgeGlowMod() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModParticles.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerParticles);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        // Client setup
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ConfigLoader());
    }

    public void registerParticles(RegisterParticleProvidersEvent event) {
        // Register without SpriteSet
        event.registerSpecial(ModParticles.GLOW.get(), new GlowParticle.Provider(null));
    }
}
