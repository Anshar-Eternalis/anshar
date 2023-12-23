package com.lgmrszd.anshar;

import com.lgmrszd.anshar.freq.IBeaconComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class FrequencyIdentifier implements IBeaconComponent {
    private List<List<Identifier>> topBlocks;
    private List<List<Identifier>> bottomBlocks;
    private int level;

    public FrequencyIdentifier() {
        level = 0;
        topBlocks = Collections.emptyList();
        bottomBlocks = Collections.emptyList();
    }

    public int getLevel() {
        return level;
    }

    public void rescanPyramid(World world, int x, int y, int z, int level) {
        this.level = level;
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
        // Tob blocks (aka edges of upper layers)
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
                            Registries.BLOCK.getId(
                                    world.getBlockState(
                                            new BlockPos(
                                                    level_x_abs,
                                                    level_y_abs,
                                                    level_z_abs
                                            )
                                    ).getBlock()
                            )
                    );
                }
        }

        // Bottom blocks (bottom layer)
        int level_y_abs = y - level;
        for (int layer_x = 0; layer_x <= level * 2; layer_x++)
            for (int layer_z = 0; layer_z <= level * 2; layer_z++) {
                int level_x_abs = layer_x + x - level;
                int level_z_abs = layer_z + z - level;
                bottomBlocks.get(layer_x).set(layer_z,
                        Registries.BLOCK.getId(
                                world.getBlockState(
                                        new BlockPos(
                                                level_x_abs,
                                                level_y_abs,
                                                level_z_abs
                                        )
                                ).getBlock()
                        )
                );
            }

    }

    public int arraysHashCode() {
        int blockHash = topBlocks.hashCode();
        blockHash = 31 * blockHash + bottomBlocks.hashCode();
        return blockHash;
    }

    private static List<Identifier> flattenList(List<List<Identifier>> topBlocks, List<List<Identifier>> bottomBlocks) {
//        int size = level == 1 ? 9 :
//                (level * 2 + 1) * (level * 2 + 1) + (level * 2 + 3) * (level * 2 + 3);
        List<Identifier> flattened = new ArrayList<>(topBlocks.size() + bottomBlocks.size());
        bottomBlocks.forEach(flattened::addAll);
        topBlocks.forEach(flattened::addAll);
        return flattened;
    }

    public List<Identifier> flattenList() {
        return flattenList(topBlocks, bottomBlocks);
    }

    public void fromFlattenedList(List<Identifier> flattenedList, int level) {
        int size = flattenedList.size();
        if (size < 9)
            throw new RuntimeException("Trying to unflatten list smaller than 9");
        if (size == 9 && level != 1)
            throw new RuntimeException(String.format("Unflattening error: wrong size %d for level %d", size, level));
        if (size > 9 && (8*level*level + 2) != size)
            throw new RuntimeException(String.format("Unflattening error: wrong size %d for level %d", size, level));

        int bottomEdge = level * 2 + 1;
        bottomBlocks = new ArrayList<>(bottomEdge);
        for (int i = 0; i < bottomEdge; i++) {
            List<Identifier> slice = flattenedList.subList(i * bottomEdge, (i + 1) * bottomEdge);
            bottomBlocks.add(new ArrayList<>(slice));
        }
        if (size == 9) {
            topBlocks = Collections.emptyList();
            return;
        }
        int offset = bottomEdge * bottomEdge;
        int topEdge = bottomEdge - 2;
        topBlocks = new ArrayList<>(topEdge);
        for (int i = 0; i < topEdge; i++) {
            List<Identifier> slice = flattenedList.subList(i * topEdge + offset, (i + 1) * topEdge + offset);
            topBlocks.add(new ArrayList<>(slice));
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        level = tag.getInt("level");
        String blockList = tag.getString("blocks");
        List<Identifier> flatList = Arrays.stream(blockList.split(","))
                .map(Identifier::tryParse)
                .toList();
        fromFlattenedList(flatList, level);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        List<Identifier> flatList = flattenList();
        String blockList = flatList.stream()
                .map(Identifier::toString)
                .collect(StringBuilder::new, (builder, str) -> builder.append(str).append(","), StringBuilder::append)
                .toString();
        tag.putInt("level", level);
        tag.putString("blocks", blockList);
    }
}
