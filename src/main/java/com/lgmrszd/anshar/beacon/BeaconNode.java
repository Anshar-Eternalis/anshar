package com.lgmrszd.anshar.beacon;

import java.io.Serializable;
import java.util.Optional;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;


public class BeaconNode implements Serializable {
    private final String name = "placeholder";
    private final DyeColor color = DyeColor.WHITE;
    private final BlockPos pos;


    public BeaconNode(BlockPos pos){
        // should really be passed a BeaconBlockEntity to construct, but you'll need to make frequencynetwork changes to allow that first
        this.pos = pos;
    }
    public String getName() {return name;};
    public DyeColor getColor() {return color;};
    public BlockPos getPos() {return pos;};
    public Optional<BeaconBlockEntity> getBeacon() {return Optional.empty();};
}
