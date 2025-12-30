package net.jules.edge_glow.logic;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class EdgeDetector {

    /**
     * Scans the image and returns a list of UV coordinates (normalized 0-1) corresponding to edge pixels.
     * An edge pixel is defined as a non-transparent pixel adjacent to a transparent one.
     */
    public static List<Vector2f> detectEdges(NativeImage image, float alphaThreshold, int densityFilter) {
        List<Vector2f> edges = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isPixelVisible(image, x, y, alphaThreshold)) {
                    if (isEdge(image, x, y, width, height, alphaThreshold)) {
                        // Normalize coordinates to 0-1 range
                        // We add 0.5 to center the particle on the pixel
                        float u = (x + 0.5f) / (float) width;
                        float v = (y + 0.5f) / (float) height;
                        edges.add(new Vector2f(u, v));
                    }
                }
            }
        }

        // Simple density reduction if needed (though usually better handled at spawn time)
        if (densityFilter > 1) {
             List<Vector2f> filtered = new ArrayList<>();
             for (int i = 0; i < edges.size(); i++) {
                 if (i % densityFilter == 0) filtered.add(edges.get(i));
             }
             return filtered;
        }

        return edges;
    }

    private static boolean isPixelVisible(NativeImage image, int x, int y, float alphaThreshold) {
        int color = image.getPixelRGBA(x, y);
        int alpha = (color >> 24) & 0xFF;
        return alpha > (alphaThreshold * 255);
    }

    private static boolean isEdge(NativeImage image, int x, int y, int w, int h, float alphaThreshold) {
        // Check 4 neighbors
        if (x > 0 && !isPixelVisible(image, x - 1, y, alphaThreshold)) return true;
        if (x < w - 1 && !isPixelVisible(image, x + 1, y, alphaThreshold)) return true;
        if (y > 0 && !isPixelVisible(image, x, y - 1, alphaThreshold)) return true;
        if (y < h - 1 && !isPixelVisible(image, x, y + 1, alphaThreshold)) return true;

        // Border pixels are implicitly edges
        if (x == 0 || x == w - 1 || y == 0 || y == h - 1) return true;

        return false;
    }
}
