package net.jules.edge_glow.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import java.lang.reflect.Field;
import java.util.Optional;

public class TexturePixelReader {

    private static Field originalImageField;

    /**
     * Reads the pixel data from a TextureAtlasSprite.
     */
    public static Optional<NativeImage> getImageFromSprite(TextureAtlasSprite sprite) {
        try {
            if (sprite.contents() == null) return Optional.empty();

            if (originalImageField == null) {
                try {
                    originalImageField = SpriteContents.class.getDeclaredField("originalImage");
                } catch (NoSuchFieldException e) {
                    // Try SRG name
                    originalImageField = SpriteContents.class.getDeclaredField("f_243831_");
                }
                originalImageField.setAccessible(true);
            }

            NativeImage image = (NativeImage) originalImageField.get(sprite.contents());
            return Optional.ofNullable(image);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
