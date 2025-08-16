package com.lgmrszd.anshar.beacon;

import java.util.Optional;

import com.lgmrszd.anshar.transport.JumpPayload;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;

/**
 * Immutable Beacon information storage
 */
public class BeaconNode {
    private final Text name;
    private final int color;
    private final BlockPos pos;

    public BeaconNode(IBeaconComponent beaconComponent) {
        this.pos = beaconComponent.getBeaconPos();
        this.name = beaconComponent.getName();
        this.color = beaconComponent.topColor();
    }

    private BeaconNode(BlockPos pos, Text name, int color) {
        this.pos = pos;
        this.name = name;
        this.color = color;
    }

    public static BeaconNode fromNBT(NbtCompound tag, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new BeaconNode(
                BlockPos.fromLong(tag.getLong("pos")),
                Text.Serialization.fromJson(tag.getString("name"), wrapperLookup),
                tag.getInt("color")
        );
    }

    public static BeaconNode fromJumpPayload(JumpPayload payload) {
        return new BeaconNode(
                payload.blockPos(),
                payload.name(),
                payload.color()
        );
    }

    public NbtCompound toNBT(RegistryWrapper.WrapperLookup wrapperLookup) {
        var tag = new NbtCompound();
        tag.putLong("pos", pos.asLong());
        tag.putString("name", Text.Serialization.toJsonString(this.name, wrapperLookup));
        tag.putInt("color", color);
        return tag;
    }

    public static BeaconNode makeFake(BlockPos pos) {
        return new BeaconNode(pos, Text.literal("?????"), ColorHelper.getArgb(255, 255, 255));
    }

    public Text getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Optional<BeaconBlockEntity> getBeacon() {
        return Optional.empty();
    }
}
