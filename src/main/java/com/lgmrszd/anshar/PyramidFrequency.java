package com.lgmrszd.anshar;

public class PyramidFrequency {
    private int baseHash;

    public PyramidFrequency(int baseHash) {
        this.baseHash = baseHash;
    }

    public int getBaseHash() {
        return baseHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        return obj instanceof PyramidFrequency other && this.baseHash == other.baseHash;
    }
}
