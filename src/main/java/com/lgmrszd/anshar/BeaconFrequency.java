package com.lgmrszd.anshar;

import com.lgmrszd.anshar.freq.IBeaconFrequency;

public class BeaconFrequency implements IBeaconFrequency {
    private final int baseHash;

    public BeaconFrequency(int baseHash) {
        this.baseHash = baseHash;
    }

    @Override
    public int hashCode() {
        return baseHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        return obj instanceof BeaconFrequency other && this.baseHash == other.baseHash;
    }
}
