package com.lgmrszd.anshar.network;

import com.lgmrszd.anshar.frequency.IFrequencyIdentifier;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class Network {
    private UUID id;
    private IFrequencyIdentifier IFreqID;

    public Network(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void readFromNbt(NbtCompound tag) {

    }

    public void writeToNbt(NbtCompound tag) {

    }

}
