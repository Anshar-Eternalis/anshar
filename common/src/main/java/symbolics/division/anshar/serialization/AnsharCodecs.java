package symbolics.division.anshar.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public final class AnsharCodecs {
    private static Codec<Float> floatRange(float min, float max) {
        return ExtraCodecs.validate(Codec.FLOAT, value ->
            value >= min && value <= max ? DataResult.success(value) : DataResult.error(() -> "Value must be between " + min + " and " + max)
        );
    }

    public static final Codec<List<Float>> FLOAT_COLOR_RGB = ExtraCodecs.validate(
            floatRange(0f, 1f).listOf(),
            c -> c.size() == 3 ? DataResult.success(c)
                               : DataResult.error(() -> "RGB Color must have exactly 3 components")
    );
}
