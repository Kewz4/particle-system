package net.jules.edge_glow.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.jules.edge_glow.init.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3f;

public class GlowParticleOptions implements ParticleOptions {
    // r, g, b (0-1) for start and end
    private final Vector3f startColor;
    private final Vector3f endColor;

    public GlowParticleOptions(Vector3f startColor, Vector3f endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public Vector3f getStartColor() { return startColor; }
    public Vector3f getEndColor() { return endColor; }

    public static final Codec<GlowParticleOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.listOf().fieldOf("start_color").forGetter(o -> java.util.List.of(o.startColor.x, o.startColor.y, o.startColor.z)),
            Codec.FLOAT.listOf().fieldOf("end_color").forGetter(o -> java.util.List.of(o.endColor.x, o.endColor.y, o.endColor.z))
    ).apply(instance, (start, end) -> new GlowParticleOptions(
            new Vector3f(start.get(0), start.get(1), start.get(2)),
            new Vector3f(end.get(0), end.get(1), end.get(2))
    )));

    public static final Deserializer<GlowParticleOptions> DESERIALIZER = new Deserializer<GlowParticleOptions>() {
        @Override
        public GlowParticleOptions fromCommand(ParticleType<GlowParticleOptions> type, StringReader reader) throws CommandSyntaxException {
             // Basic parsing not implemented for command, using default white
             return new GlowParticleOptions(new Vector3f(1,1,1), new Vector3f(1,1,1));
        }

        @Override
        public GlowParticleOptions fromNetwork(ParticleType<GlowParticleOptions> type, FriendlyByteBuf buf) {
            return new GlowParticleOptions(
                    new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()),
                    new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
            );
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ModParticles.GLOW.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(startColor.x);
        buf.writeFloat(startColor.y);
        buf.writeFloat(startColor.z);
        buf.writeFloat(endColor.x);
        buf.writeFloat(endColor.y);
        buf.writeFloat(endColor.z);
    }

    @Override
    public String writeToString() {
        return String.format("glow %f %f %f %f %f %f", startColor.x, startColor.y, startColor.z, endColor.x, endColor.y, endColor.z);
    }
}
