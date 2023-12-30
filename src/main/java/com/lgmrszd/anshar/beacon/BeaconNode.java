package com.lgmrszd.anshar.beacon;

import java.util.Optional;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.network.PacketByteBuf;
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


    public BeaconNode(BlockPos pos){
        // should really be passed a BeaconBlockEntity to construct, but you'll need to make frequencynetwork changes to allow that first
        this.pos = pos;
        this.name = Text.literal("placeholder");
        this.color = DyeColor.WHITE;
    }

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

    public static BeaconNode fromPBF(PacketByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        Text name = buffer.readText();
        DyeColor color = buffer.readEnumConstant(DyeColor.class);
        return new BeaconNode(pos, name, color);
    }

    public void toPBF(PacketByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeText(name);
        buffer.writeEnumConstant(color);
    }

    public Text getName() {return name;}
    public DyeColor getColor() {return color;}
    public BlockPos getPos() {return pos;}
    public Optional<BeaconBlockEntity> getBeacon() {return Optional.empty();}
}
