package com.lgmrszd.anshar.frequency;

import java.util.Set;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import com.lgmrszd.anshar.storage.EmbeddedStorage;

public class FrequencyNetwork {
    private UUID id;
    private IFrequencyIdentifier FreqID;
    private EmbeddedStorage storage;
    private Set<BlockPos> beacons;

    public FrequencyNetwork(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public IFrequencyIdentifier getFreqID() {
        return FreqID;
    }

    public Set<BlockPos> getBeacons(){
        return this.beacons;
    }

    public EmbeddedStorage getStorage(){
        return this.storage;
    }

    public void readFromNbt(NbtCompound tag) {

    }

    public void writeToNbt(NbtCompound tag) {

    }

}
