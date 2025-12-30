package net.jules.edge_glow.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.jules.edge_glow.EdgeGlowMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class ConfigLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final Map<Item, GlowConfig> ITEM_RULES = new HashMap<>();

    public ConfigLoader() {
        super(GSON, "edge_glow/glow_rules");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ITEM_RULES.clear();
        EdgeGlowMod.LOGGER.info("EdgeGlow: Loading rules from data pack...");

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                GlowConfig config = GSON.fromJson(entry.getValue(), GlowConfig.class);
                if (config.items != null) {
                    for (ResourceLocation itemId : config.items) {
                        Item item = ForgeRegistries.ITEMS.getValue(itemId);
                        if (item != null) {
                            ITEM_RULES.put(item, config);
                            EdgeGlowMod.LOGGER.info("EdgeGlow: Registered rule for item {}", itemId);
                        } else {
                             EdgeGlowMod.LOGGER.warn("EdgeGlow: Item not found for rule {}: {}", id, itemId);
                        }
                    }
                }
            } catch (Exception e) {
                EdgeGlowMod.LOGGER.error("EdgeGlow: Failed to load glow rule: {}", id, e);
            }
        }
        EdgeGlowMod.LOGGER.info("EdgeGlow: Loaded {} item glow rules.", ITEM_RULES.size());
    }
}
