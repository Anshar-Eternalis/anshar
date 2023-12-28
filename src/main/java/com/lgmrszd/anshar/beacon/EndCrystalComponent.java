package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class EndCrystalComponent implements IEndCrystalComponent {
    private static final int MAX_DISTANCE = 16;
    private BlockPos beaconPos;
    private final EndCrystalEntity endCrystal;

    private boolean shouldUpdateBeacon;

    public EndCrystalComponent(EndCrystalEntity endCrystal) {
        this.endCrystal = endCrystal;
        shouldUpdateBeacon = true;
    }

    private void tryUpdateBeacon() {
        if (!shouldUpdateBeacon) return;
        World world = endCrystal.getWorld();
        if (world == null) return;
        BlockPos crystalPos = endCrystal.getBlockPos();
        NetworkManagerComponent.KEY.get(world.getLevelProperties()).getNearestConnectedBeacon(world, crystalPos).ifPresent(beaconBlockEntity -> {
            BlockPos pos = beaconBlockEntity.getPos();
            if (!pos.isWithinDistance(crystalPos, MAX_DISTANCE)) return;
            beaconPos = pos;
            endCrystal.setBeamTarget(pos.offset(Direction.DOWN, 2));
        });
        shouldUpdateBeacon = false;
    }

    @Override
    public void serverTick() {
        tryUpdateBeacon();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

    }

    @Override
    public Optional<BlockPos> getConnectedBeacon() {
        return Optional.ofNullable(beaconPos);
    }
}
