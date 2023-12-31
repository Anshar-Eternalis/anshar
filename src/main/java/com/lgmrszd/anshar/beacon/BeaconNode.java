package com.lgmrszd.anshar.beacon;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

/**
 * Immutable Beacon information storage
 */
public class BeaconNode {
    private final Text name;
    private final float[] color;
    private final BlockPos pos;

    public BeaconNode(IBeaconComponent beaconComponent) {
        this.pos = beaconComponent.getBeaconPos();
        this.name = beaconComponent.getName();
        float[] color = beaconComponent.topColor();
        if (color != null && color.length == 3){
            this.color = Arrays.copyOf(color, 3);
        } else {
            this.color = new float[]{0, 0, 0};
        }
        
    }

    private BeaconNode(BlockPos pos, Text name, float[] color) {
        this.pos = pos;
        this.name = name;
        this.color = color;
    }

    public static BeaconNode fromNBT(NbtCompound tag) {
        return new BeaconNode(
            BlockPos.fromLong(tag.getLong("pos")),
            Text.Serialization.fromJson(tag.getString("name")),
            new float[]{tag.getFloat("r"), tag.getFloat("g"), tag.getFloat("b")}
        );
    }

    public NbtCompound toNBT() {
        var tag = new NbtCompound();
        tag.putLong("pos", pos.asLong());
        tag.putString("name", Text.Serialization.toJsonString(this.name));
        tag.putFloat("r", color[0]);
        tag.putFloat("g", color[1]);
        tag.putFloat("b", color[2]);
        return tag;
    }

    public Text getName() {return name;}
    public float[] getColor() {return color;}
    public BlockPos getPos() {return pos;}
    public Optional<BeaconBlockEntity> getBeacon() {return Optional.empty();}
}
