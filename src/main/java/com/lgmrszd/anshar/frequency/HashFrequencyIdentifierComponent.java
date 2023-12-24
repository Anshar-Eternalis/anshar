package com.lgmrszd.anshar.frequency;

import net.minecraft.nbt.NbtCompound;

public final class HashFrequencyIdentifierComponent implements IFrequencyIdentifierComponent {

    private static final String NBT_KEY = "frequency_id_hash";

    private IFrequencyIdentifier id;
    
    public HashFrequencyIdentifierComponent(){ id = NullFrequencyIdentifier.get(); }
    
    public void set(HashFrequencyIdentifier value){ id = value; }
    public void set(int hash){ id = new HashFrequencyIdentifier(hash); }
    public void clear(){ id = NullFrequencyIdentifier.get(); }
    public IFrequencyIdentifier get() { return id; }

    @Override
    public void readFromNbt(NbtCompound tag) {
        int v = tag.getInt(NBT_KEY);
        if (v != 0) this.set(v);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (id.isValid()) tag.putInt(NBT_KEY, ((HashFrequencyIdentifier)id).hash());
    }
    
}
