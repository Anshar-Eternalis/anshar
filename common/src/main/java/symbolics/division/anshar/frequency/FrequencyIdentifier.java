package symbolics.division.anshar.frequency;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface FrequencyIdentifier {

    Codec<FrequencyIdentifier> CODEC = ResourceLocation.CODEC.dispatch(FrequencyIdentifier::type,
        id -> PyramidFrequencyIdentifier.CODEC // create registry/lookup if we add more
    );

    boolean isValid();

    default boolean isValidInDimension(ResourceKey<Level> dim) {return isValid();}

    ResourceLocation type();

}
