package net.jules.edge_glow.config;

import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class GlowConfig {
    public List<ResourceLocation> items;
    public ResourceLocation particle;
    public String color_matcher; // Optional hex
    public String gradient_start; // Hex Color
    public String gradient_end;   // Hex Color
    public float spawn_rate;
    public float[] offset; // [x, y, z]
    public float alpha_threshold;

    // Defaults
    public GlowConfig() {
        this.particle = new ResourceLocation("edge_glow", "glow");
        this.spawn_rate = 0.1f;
        this.offset = new float[]{0.0f, 0.0f, 0.0f};
        this.alpha_threshold = 0.1f;
        this.gradient_start = "#FFFFFF";
        this.gradient_end = "#FFFFFF";
    }
}
