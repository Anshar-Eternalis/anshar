package com.lgmrszd.anshar;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PyramidFrequencer {
    private final List<List<Block>> topBlocks;
    private final List<List<Block>> bottomBlocks;
    private final PyramidFrequency frequency;

    public PyramidFrequencer(World world, int x, int y, int z, int level) {
        if (level < 1)
            throw new RuntimeException("Can't have Beacon Frequency of level less than 1");

        // Set up arrays

        if (level == 1) {
            topBlocks = Collections.emptyList();
        } else {
            int uplevel = level - 1;
            topBlocks = new ArrayList<>(uplevel * 2 + 1);
            for (int i = 0; i < uplevel * 2 + 1; i++)
                topBlocks.add(new ArrayList<>(Collections.nCopies(uplevel * 2 + 1, null)));
        }
        bottomBlocks = new ArrayList<>(level * 2 + 1);
        for (int i = 0; i < level * 2 + 1; i++)
            bottomBlocks.add(new ArrayList<>(Collections.nCopies(level * 2 + 1, null)));

        // Scan blocks
        if (level > 1) {
            int up_level = level - 1;
            for (int layer_x = 0; layer_x <= up_level * 2; layer_x++)
                for (int layer_z = 0; layer_z <= up_level * 2; layer_z++) {
                    int level_y_abs = y - up_level + Math.min(
                            up_level - Math.abs(layer_x - up_level),
                            up_level - Math.abs(layer_z - up_level)
                    );
                    int level_x_abs = layer_x + x - up_level;
                    int level_z_abs = layer_z + z - up_level;
                    topBlocks.get(layer_x).set(layer_z,
                            world.getBlockState(
                                    new BlockPos(
                                            level_x_abs,
                                            level_y_abs,
                                            level_z_abs
                                    )
                            ).getBlock()
                    );
                }
        }

        int level_y_abs = y - level;
        for (int layer_x = 0; layer_x <= level * 2; layer_x++)
            for (int layer_z = 0; layer_z <= level * 2; layer_z++) {
                int level_x_abs = layer_x + x - level;
                int level_z_abs = layer_z + z - level;
                bottomBlocks.get(layer_x).set(layer_z,
                        world.getBlockState(
                                new BlockPos(
                                        level_x_abs,
                                        level_y_abs,
                                        level_z_abs
                                )
                        ).getBlock()
                );
            }

        frequency = recalculateFrequency();
    }

    private PyramidFrequency recalculateFrequency() {
        int blockHash = topBlocks.hashCode();
        blockHash = 31 * blockHash + bottomBlocks.hashCode();
        return new PyramidFrequency(blockHash);
    }

    public PyramidFrequency getFrequency() {
        return frequency;
    }
}
