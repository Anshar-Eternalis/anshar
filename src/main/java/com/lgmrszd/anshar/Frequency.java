package com.lgmrszd.anshar;

public class Frequency {
    private int baseHash;

    public Frequency(int baseHash) {
        this.baseHash = baseHash;
    }

    public int getBaseHash() {
        return baseHash;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        return obj instanceof Frequency other && this.baseHash == other.baseHash;
//    }
}
