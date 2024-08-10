package symbolics.division.anshar.frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import symbolics.division.anshar.Anshar;

import static symbolics.division.anshar.Anshar.LOGGER;

public final class PyramidFrequencyIdentifier implements FrequencyIdentifier {

    public static final Codec<PyramidFrequencyIdentifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("blocks").forGetter(pid -> pid.pyramidArrangements.get(0)),
            Codec.INT.fieldOf("level").forGetter(pid -> pid.level),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(pid -> pid.dimension)
    ).apply(instance, PyramidFrequencyIdentifier::fromFlattened));

    private static List<ResourceLocation> flatten(List<List<ResourceLocation>> topBlocks, List<List<ResourceLocation>> bottomBlocks) {
//        int size = level == 1 ? 9 :
//                (level * 2 + 1) * (level * 2 + 1) + (level * 2 + 3) * (level * 2 + 3);
        List<ResourceLocation> flattened = new ArrayList<>(topBlocks.size() + bottomBlocks.size());
        bottomBlocks.forEach(flattened::addAll);
        topBlocks.forEach(flattened::addAll);
        return flattened;
    }

    private static PyramidFrequencyIdentifier fromFlattened(List<ResourceLocation> flattened, int level, ResourceKey<Level> dimension) {
        int size = flattened.size();
        if (size < 9){
            LOGGER.error(String.format("Unflattening error: wrong size %d for level %d", size, level));
            return null;
        }
        if (size == 9 && level != 1) {
            LOGGER.error(String.format("Unflattening error: wrong size %d for level %d", size, level));
            return null;
        }
        if (size > 9 && (8*level*level + 2) != size) {
            LOGGER.error(String.format("Unflattening error: wrong size %d for level %d", size, level));
            return null;
        }

        int bottomEdge = level * 2 + 1;
        int bottomEdgeMinus1 = bottomEdge - 1;
        List<List<List<ResourceLocation>>> bottomBlocks = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            List<List<ResourceLocation>> bottomBlocks2d = new ArrayList<>(bottomEdge);
            for (int j = 0; j < bottomEdge; j++) {
                bottomBlocks2d.add(new ArrayList<>(Collections.nCopies(bottomEdge, null)));
            }
            bottomBlocks.add(bottomBlocks2d);
        }

        int bottomBlocksSize = bottomEdge * bottomEdge;
        for (int i = 0; i < bottomBlocksSize; i++) {
            ResourceLocation blockId = flattened.get(i);
            // We store rows
            // X is row number
            // Z is column number in the row
            int x = i / bottomEdge;
            int z = i % bottomEdge;
            bottomBlocks.get(0).get(x).set(z, blockId);
            bottomBlocks.get(1).get(z).set(bottomEdgeMinus1-x, blockId);
            bottomBlocks.get(2).get(bottomEdgeMinus1-x).set(bottomEdgeMinus1-z, blockId);
            bottomBlocks.get(3).get(bottomEdgeMinus1-z).set(x, blockId);
        }

        List<List<List<ResourceLocation>>> topBlocks = new ArrayList<>(4);
        if (level == 1) {
            for (int i = 0; i < 4; i++) {
                topBlocks.add(Collections.emptyList());
            }
        } else {
            int topEdge = bottomEdge - 2;
            int topEdgeMinus1 = topEdge - 1;
            int topBlocksSize = topEdge * topEdge;

            for (int i = 0; i < 4; i++) {
                List<List<ResourceLocation>> topBlocks2d = new ArrayList<>(topEdge);
                for (int j = 0; j < topEdge; j++) {
                    topBlocks2d.add(new ArrayList<>(Collections.nCopies(topEdge, null)));
                }
                topBlocks.add(topBlocks2d);
            }

            for (int i = 0; i < topBlocksSize; i++) {
                ResourceLocation blockId = flattened.get(i + bottomBlocksSize);
                int x = i / topEdge;
                int z = i % topEdge;
                topBlocks.get(0).get(x).set(z, blockId);
                topBlocks.get(1).get(z).set(topEdgeMinus1-x, blockId);
                topBlocks.get(2).get(topEdgeMinus1-x).set(topEdgeMinus1-z, blockId);
                topBlocks.get(3).get(topEdgeMinus1-z).set(x, blockId);
            }
        }

        List<List<ResourceLocation>> pyramidArrangements = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            pyramidArrangements.add(flatten(topBlocks.get(i), bottomBlocks.get(i)));
        }

        return new PyramidFrequencyIdentifier(pyramidArrangements, level, dimension);
    }

    public static PyramidFrequencyIdentifier scanForPyramid(Level world, BlockPos pos, int level) {
        if (level < 1) {
            LOGGER.error("Tried to create PyramidFrequencyIdentifier with level {}", level);
            return null;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int bottomEdge = level * 2 + 1;

        List<List<List<ResourceLocation>>> bottomBlocks = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            List<List<ResourceLocation>> bottomBlocks2d = new ArrayList<>(bottomEdge);
            for (int j = 0; j < bottomEdge; j++) {
                bottomBlocks2d.add(new ArrayList<>(Collections.nCopies(bottomEdge, null)));
            }
            bottomBlocks.add(bottomBlocks2d);
        }

        int level_y_abs = y - level;
        int bottomEdgeMinus1 = bottomEdge - 1;
        for (int layer_x = 0; layer_x < bottomEdge; layer_x++)
            for (int layer_z = 0; layer_z < bottomEdge; layer_z++) {
                int level_x_abs = layer_x + x - level;
                int level_z_abs = layer_z + z - level;
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(
                        world.getBlockState(
                                new BlockPos(
                                        level_x_abs,
                                        level_y_abs,
                                        level_z_abs
                                )
                        ).getBlock()
                );
                bottomBlocks.get(0).get(layer_x).set(layer_z, blockId);
                bottomBlocks.get(1).get(layer_z).set(bottomEdgeMinus1-layer_x, blockId);
                bottomBlocks.get(2).get(bottomEdgeMinus1-layer_x).set(bottomEdgeMinus1-layer_z, blockId);
                bottomBlocks.get(3).get(bottomEdgeMinus1-layer_z).set(layer_x, blockId);
            }

//        Identifier[][][] topBlocks;
        List<List<List<ResourceLocation>>> topBlocks = new ArrayList<>(4);
        if (level > 1) {
            int up_level = level - 1;
            int topEdge = level * 2 - 1;

            for (int i = 0; i < 4; i++) {
                List<List<ResourceLocation>> topBlocks2d = new ArrayList<>(topEdge);
                for (int j = 0; j < topEdge; j++) {
                    topBlocks2d.add(new ArrayList<>(Collections.nCopies(topEdge, null)));
                }
                topBlocks.add(topBlocks2d);
            }

            int topEdgeMinus1 = topEdge - 1;

            for (int layer_x = 0; layer_x < topEdge; layer_x++)
                for (int layer_z = 0; layer_z < topEdge; layer_z++) {
                    level_y_abs = y - up_level + Math.min(
                            up_level - Math.abs(layer_x - up_level),
                            up_level - Math.abs(layer_z - up_level)
                    );
                    if (level_y_abs == y) level_y_abs = y - 1;
                    int level_x_abs = layer_x + x - up_level;
                    int level_z_abs = layer_z + z - up_level;

                    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(
                            world.getBlockState(
                                    new BlockPos(
                                            level_x_abs,
                                            level_y_abs,
                                            level_z_abs
                                    )
                            ).getBlock()
                    );
                    topBlocks.get(0).get(layer_x).set(layer_z, blockId);
                    topBlocks.get(1).get(layer_z).set(topEdgeMinus1-layer_x, blockId);
                    topBlocks.get(2).get(topEdgeMinus1-layer_x).set(topEdgeMinus1-layer_z, blockId);
                    topBlocks.get(3).get(topEdgeMinus1-layer_z).set(layer_x, blockId);
                }

        } else {
            for (int i = 0; i < 4; i++) {
                topBlocks.add(Collections.emptyList());
            }
        }

        List<List<ResourceLocation>> pyramidArrangements = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            pyramidArrangements.add(flatten(topBlocks.get(i), bottomBlocks.get(i)));
        }

        return new PyramidFrequencyIdentifier(pyramidArrangements, level, world.dimension());
    }

    private final int level;
    private final List<List<ResourceLocation>> pyramidArrangements;
    private final ResourceKey<Level> dimension;

    private PyramidFrequencyIdentifier(List<List<ResourceLocation>> pyramidArrangements, int level, ResourceKey<Level> dimension) {
        this.pyramidArrangements = pyramidArrangements;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isValidInDimension(ResourceKey<Level> dim) {
        return this.dimension.equals(dim);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PyramidFrequencyIdentifier other)) return false;
        if (!dimension.equals(other.dimension)) return false;
        List<ResourceLocation> otherArrangement = other.pyramidArrangements.get(0);
        for (List<ResourceLocation> pyramidArrangement: pyramidArrangements) {
            if (pyramidArrangement.equals(otherArrangement)) return true;
        }
        return false;
    }

    @Override
    public ResourceLocation type() {
        return Anshar.id("pyramid");
    }

    private String getBlockList() {
        List<ResourceLocation> flattened = pyramidArrangements.get(0);
        return flattened.stream()
                .map(ResourceLocation::toString)
                .collect(StringBuilder::new, (builder, str) -> builder.append(str).append(","), StringBuilder::append)
                .toString();
    }

}
