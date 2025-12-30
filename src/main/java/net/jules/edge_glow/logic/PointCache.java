package net.jules.edge_glow.logic;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PointCache {
    private static final Map<ResourceLocation, List<Vector2f>> CACHE = new ConcurrentHashMap<>();

    public static void put(ResourceLocation spriteId, List<Vector2f> points) {
        CACHE.put(spriteId, points);
    }

    public static List<Vector2f> get(ResourceLocation spriteId) {
        return CACHE.get(spriteId);
    }

    public static boolean has(ResourceLocation spriteId) {
        return CACHE.containsKey(spriteId);
    }

    public static void clear() {
        CACHE.clear();
    }
}
