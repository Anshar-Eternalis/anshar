package com.lgmrszd.anshar.frequency;

public record HashFrequencyIdentifier (int hash) implements IFrequencyIdentifier {

    @Override
    public boolean equals(Object other){
        if (other instanceof HashFrequencyIdentifier otherFreq)
            return hash == otherFreq.hash;
        return false;
    }

    @Override
    public int hashCode(){ return hash; } // for maps, sets...

    @Override
    public boolean isValid() { return true; }
}
