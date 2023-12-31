package com.lgmrszd.anshar.beacon;

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
    private final DyeColor color;
    private final BlockPos pos;

    public BeaconNode(IBeaconComponent beaconComponent) {
        this.pos = beaconComponent.getBeaconPos();
        this.name = beaconComponent.getName();
        this.color = DyeColor.WHITE;
    }

    private BeaconNode(BlockPos pos, Text name, DyeColor color) {
        this.pos = pos;
        this.name = name;
        this.color = color;
    }

    public static BeaconNode fromNBT(NbtCompound tag) {
        DyeColor tagColor;
        try {
            tagColor = DyeColor.byId(tag.getInt("color"));
        } catch (IllegalArgumentException e) {
            tagColor = DyeColor.WHITE;
        }
        return new BeaconNode(
            BlockPos.fromLong(tag.getLong("pos")),
            Text.Serialization.fromJson(tag.getString("name")),
            tagColor
        );
    }

    public NbtCompound toNBT() {
        var tag = new NbtCompound();
        tag.putLong("pos", pos.asLong());
        tag.putString("name", Text.Serialization.toJsonString(this.name));
        tag.putInt("color", color.getId());
        return tag;
    }

    public Text getName() {return name;}
    public DyeColor getColor() {return color;}
    public BlockPos getPos() {return pos;}
    public Optional<BeaconBlockEntity> getBeacon() {return Optional.empty();}
}
