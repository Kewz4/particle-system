package net.jules.edge_glow.event;

import net.jules.edge_glow.EdgeGlowMod;
import net.jules.edge_glow.config.ConfigLoader;
import net.jules.edge_glow.config.GlowConfig;
import net.jules.edge_glow.logic.PointCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = EdgeGlowMod.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        GlowConfig config = ConfigLoader.ITEM_RULES.get(event.getItemStack().getItem());
        if (config != null) {
            String name = event.getItemStack().getHoverName().getString();
            boolean matches = true;
            String pattern = "N/A";

            if (config.name_pattern != null && !config.name_pattern.isEmpty()) {
                pattern = config.name_pattern;
                matches = name.matches(pattern);
            }

            event.getToolTip().add(Component.literal("§d[EdgeGlow] Debug Info:"));
            event.getToolTip().add(Component.literal("§7 Name: " + name));
            event.getToolTip().add(Component.literal("§7 Pattern: " + pattern));

            if (matches) {
                 event.getToolTip().add(Component.literal("§a Match: YES"));

                 // Check Edge Cache
                 try {
                     var itemRenderer = Minecraft.getInstance().getItemRenderer();
                     var model = itemRenderer.getModel(event.getItemStack(), event.getEntity().level(), event.getEntity(), 0);
                     TextureAtlasSprite sprite = model.getParticleIcon();
                     if (sprite != null) {
                         ResourceLocation spriteId = sprite.contents().name();
                         if (PointCache.has(spriteId)) {
                             int count = PointCache.get(spriteId).size();
                             event.getToolTip().add(Component.literal("§a Cached Edges: " + count));
                         } else {
                             event.getToolTip().add(Component.literal("§e Edges: Not Cached Yet"));
                         }
                     }
                 } catch (Exception e) {
                     event.getToolTip().add(Component.literal("§c Error checking sprite"));
                 }

            } else {
                 event.getToolTip().add(Component.literal("§c Match: NO"));
            }
        }
    }
}
