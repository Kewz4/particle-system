package net.jules.edge_glow.logic;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class EdgeDetector {

    public static List<Vector2f> detectEdges(NativeImage image, float alphaThreshold, int densityFilter) {
        List<Vector2f> edges = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isPixelVisible(image, x, y, alphaThreshold)) {
                    if (isEdge(image, x, y, width, height, alphaThreshold)) {
                        float u = (x + 0.5f) / (float) width;
                        float v = (y + 0.5f) / (float) height;
                        edges.add(new Vector2f(u, v));
                    }
                }
            }
        }

        if (densityFilter > 1) {
             List<Vector2f> filtered = new ArrayList<>();
             for (int i = 0; i < edges.size(); i++) {
                 if (i % densityFilter == 0) filtered.add(edges.get(i));
             }
             return filtered;
        }

        return edges;
    }

    /**
     * Fallback method that returns a simple box outline if texture reading fails.
     */
    public static List<Vector2f> getBoxFallback() {
        List<Vector2f> edges = new ArrayList<>();
        // Top and Bottom
        for (float u = 0; u <= 1.0f; u += 0.0625f) {
            edges.add(new Vector2f(u, 0.0f));
            edges.add(new Vector2f(u, 1.0f));
        }
        // Left and Right
        for (float v = 0; v <= 1.0f; v += 0.0625f) {
            edges.add(new Vector2f(0.0f, v));
            edges.add(new Vector2f(1.0f, v));
        }
        return edges;
    }

    private static boolean isPixelVisible(NativeImage image, int x, int y, float alphaThreshold) {
        int color = image.getPixelRGBA(x, y);
        int alpha = (color >> 24) & 0xFF;
        return alpha > (alphaThreshold * 255);
    }

    private static boolean isEdge(NativeImage image, int x, int y, int w, int h, float alphaThreshold) {
        if (x > 0 && !isPixelVisible(image, x - 1, y, alphaThreshold)) return true;
        if (x < w - 1 && !isPixelVisible(image, x + 1, y, alphaThreshold)) return true;
        if (y > 0 && !isPixelVisible(image, x, y - 1, alphaThreshold)) return true;
        if (y < h - 1 && !isPixelVisible(image, x, y + 1, alphaThreshold)) return true;
        if (x == 0 || x == w - 1 || y == 0 || y == h - 1) return true;
        return false;
    }
}
