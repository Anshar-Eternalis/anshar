package com.lgmrszd.anshar.EndCrystal;

import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class EndCrystalComponent implements IEndCrystalComponent {
    private BlockPos beaconPos;
    private final EndCrystalEntity endCrystal;

    public EndCrystalComponent(EndCrystalEntity endCrystal) {
        this.endCrystal = endCrystal;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

    }

    @Override
    public Optional<BlockPos> getConnectedBeacon() {
        return Optional.empty();
    }
}
