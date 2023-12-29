package com.lgmrszd.anshar.frequency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import com.lgmrszd.anshar.storage.EmbeddedStorage;

public class FrequencyNetwork {
    private UUID id;
    private IFrequencyIdentifier freqID;
    private EmbeddedStorage storage;
    private Set<BlockPos> beacons;

    public FrequencyNetwork(UUID id, IFrequencyIdentifier FreqID) {
        this.id = id;
        this.freqID = FreqID;
        beacons = new HashSet<>();
    }

    public UUID getId() {
        return id;
    }

    public IFrequencyIdentifier getFreqID() {
        return freqID;
    }

    public Set<BlockPos> getBeacons() {
        return Collections.unmodifiableSet(this.beacons);
    }

    // TODO: find a way to make it protected?
    public boolean removeBeacon (BlockPos beaconPos) {
        return beacons.remove(beaconPos);
    }

    protected boolean addBeacon (BlockPos beaconPos) {
        return beacons.add(beaconPos);
    }

    public EmbeddedStorage getStorage(){
        if (this.storage == null){
            this.storage = new EmbeddedStorage();
        }
        return this.storage;
    }

//    public void readFromNbt(NbtCompound tag) {
//
//    }

    // TODO: store key as constant
    // TODO: generalize to an interface
    public static FrequencyNetwork fromNbt(UUID id, NbtCompound tag) {
        IFrequencyIdentifier freqID = PyramidFrequencyIdentifier.fromNbt(tag.getCompound("frequency"));
        var network = new FrequencyNetwork(id, freqID);
        network.getStorage().readNbtList(tag.getList("storage", NbtCompound.COMPOUND_TYPE));
        return network;
    }

    public void writeToNbt(NbtCompound tag) {
        NbtCompound pfIDTag = new NbtCompound();
        // TODO improve this
        if (freqID instanceof PyramidFrequencyIdentifier pfID) {
            pfID.toNbt(pfIDTag);
            tag.put("frequency", pfIDTag);
        } else
            tag.putInt("frequency", freqID.hashCode());
        tag.put("storage", this.getStorage().toNbtList());
    }

}
