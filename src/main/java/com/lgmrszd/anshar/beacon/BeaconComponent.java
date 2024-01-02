package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.frequency.*;
import com.lgmrszd.anshar.mixin.accessor.BeaconBlockEntityAccessor;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.*;

public class BeaconComponent implements IBeaconComponent {
    private final BeaconBlockEntity beaconBlockEntity;

    private IFrequencyIdentifier pyramidFrequency;

    private FrequencyNetwork frequencyNetwork;
    private boolean active;
    private int level;
    private Vec3d particleVec;

    private float[] cachedColor;

    public BeaconComponent(BeaconBlockEntity beaconBlockEntity) {
        this.beaconBlockEntity = beaconBlockEntity;
        level = 0;
        particleVec = new Vec3d(1, 0, 0);
        cachedColor = new float[]{0, 0, 0};
        pyramidFrequency = NullFrequencyIdentifier.get();
        active = false;
    }

    private IFrequencyIdentifier rescanPyramid() {
        World world = beaconBlockEntity.getWorld();

        BlockPos pos = beaconBlockEntity.getPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        level = BeaconBlockEntityAccessor.updateLevel(world, x, y, z);
        return level == 0 ? NullFrequencyIdentifier.get() :
                PyramidFrequencyIdentifier.scanForPyramid(world, getBeaconPos(), level);
    }

    @Override
    public boolean isValid() {
        return !beaconBlockEntity.getBeamSegments().isEmpty();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Text getName() {
        return beaconBlockEntity.getName();
    }

    @Override
    public BlockPos getBeaconPos() {
        return beaconBlockEntity.getPos();
    }

    @Override
    public IFrequencyIdentifier getEffectiveFrequencyID() {
        return active ? pyramidFrequency : NullFrequencyIdentifier.get();
    }

    @Override
    public Optional<FrequencyNetwork> getFrequencyNetwork() {
        return Optional.ofNullable(frequencyNetwork);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        level = tag.getInt("level");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("level", level);
    }

    private void updateNetwork() {
        World world = beaconBlockEntity.getWorld();
        NetworkManagerComponent networkManagerComponent = NetworkManagerComponent.KEY.get(world.getLevelProperties());
        networkManagerComponent.updateBeaconNetwork(this, pyramidFrequency, frequencyNetwork1 -> {
            frequencyNetwork = frequencyNetwork1;
        });
    }

    private void activate() {
        pyramidFrequency = rescanPyramid();
        if (!pyramidFrequency.isValid()) return;
        updateNetwork();
        active = true;
    }

    private void deactivate() {
        pyramidFrequency = NullFrequencyIdentifier.get();
        updateNetwork();
        active = false;
    }

    @Override
    public void serverTick() {
        World world = beaconBlockEntity.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;
        boolean valid = isValid();
        if (valid && !active) {
            activate();
            return;
        }
        if (!valid && active) {
            deactivate();
        }
        if (valid) {
            float[] currentTopColor = getTopColor();
            if (!Arrays.equals(cachedColor, currentTopColor)) {
                cachedColor = currentTopColor;
                if (frequencyNetwork != null) frequencyNetwork.updateBeacon(this);
            }
            if (world.getTime() % 5L == 0L) {
                Vec3i pos = getBeaconPos();
                Vec3d particlePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).add(particleVec);
                particleVec = particleVec.rotateY(36f * (float) (Math.PI / 180));
                serverWorld.spawnParticles(
                        ParticleTypes.GLOW,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        1, 0, 0, 0, 0
                );
            }
            if (world.getTime() % 80L == 0L) {
                IFrequencyIdentifier newFreqID = rescanPyramid();
                if (newFreqID.isValid() && !newFreqID.equals(pyramidFrequency)) {
                    pyramidFrequency = newFreqID;
                    updateNetwork();
                }
            }
        }
    }
    private float[] getTopColor() {
        var segments = beaconBlockEntity.getBeamSegments();
        if (!segments.isEmpty()) {
            return segments.get(segments.size()-1).getColor();
        }
        return new float[]{0, 0, 0};
    }

    @Override
    public float[] topColor() {
        return cachedColor.clone();
    }
}
