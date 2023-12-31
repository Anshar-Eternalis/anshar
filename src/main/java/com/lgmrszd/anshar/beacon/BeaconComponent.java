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

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class BeaconComponent implements IBeaconComponent {
    private final BeaconBlockEntity beaconBlockEntity;

    private IFrequencyIdentifier pyramidFrequency;

    private FrequencyNetwork frequencyNetwork;
    private boolean shouldRestoreNetwork;
    private boolean isValid;
    private int level;
    private int checkBeamTicks;
    private Vec3d vec = new Vec3d(1, 0, 0);

    public BeaconComponent(BeaconBlockEntity beaconBlockEntity) {
        this.beaconBlockEntity = beaconBlockEntity;
        level = 0;
        pyramidFrequency = NullFrequencyIdentifier.get();
        isValid = false;
        shouldRestoreNetwork = false;
        checkBeamTicks = 0;
    }

    public void rescanPyramid() {
        World world = beaconBlockEntity.getWorld();
        if (world == null) return;

        BlockPos pos = beaconBlockEntity.getPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        level = BeaconBlockEntityAccessor.updateLevel(world, x, y, z);
        IFrequencyIdentifier newFreqID = level == 0 ? NullFrequencyIdentifier.get() :
                PyramidFrequencyIdentifier.scanForPyramid(world, getBeaconPos(), level);

        if (newFreqID == null || !newFreqID.isValid() || !pyramidFrequency.equals(newFreqID)) {
            LOGGER.info("Invalidating Beacon!! At {}", getBeaconPos());
            isValid = false;
            pyramidFrequency = NullFrequencyIdentifier.get();
            NetworkManagerComponent networkManagerComponent = NetworkManagerComponent.KEY.get(world.getLevelProperties());
            networkManagerComponent.updateBeaconNetwork(this, pyramidFrequency, frequencyNetwork1 -> {
                frequencyNetwork = frequencyNetwork1;
            });
            beaconBlockEntity.markDirty();
        }
    }

    private void checkBeam() {
        // TODO this
//        LOGGER.info("Checking the beam...");
    }

    @Override
    public void activate() {
        if (isValid) return;
        World world = beaconBlockEntity.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;

        BlockPos pos = beaconBlockEntity.getPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        level = BeaconBlockEntityAccessor.updateLevel(world, x, y, z);
        IFrequencyIdentifier newFreqID = level == 0 ? NullFrequencyIdentifier.get() :
                PyramidFrequencyIdentifier.scanForPyramid(world, getBeaconPos(), level);
        if (newFreqID != null && newFreqID.isValid()) {
            LOGGER.info("Activating Beacon!! At {}", getBeaconPos());
            isValid = true;
            pyramidFrequency = newFreqID;
            NetworkManagerComponent networkManagerComponent = NetworkManagerComponent.KEY.get(world.getLevelProperties());
            networkManagerComponent.updateBeaconNetwork(this, pyramidFrequency, frequencyNetwork1 -> {
                frequencyNetwork = frequencyNetwork1;
            });
            beaconBlockEntity.markDirty();
        }
    }

    private void restoreNetwork() {
        World world = beaconBlockEntity.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;
        NetworkManagerComponent networkManagerComponent = NetworkManagerComponent.KEY.get(world.getLevelProperties());
        networkManagerComponent.updateBeaconNetwork(this, pyramidFrequency, frequencyNetwork1 -> {
            frequencyNetwork = frequencyNetwork1;
        });
        shouldRestoreNetwork = false;
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
    public IFrequencyIdentifier getFrequencyID() {
        return pyramidFrequency;
    }

    @Override
    public Optional<FrequencyNetwork> getFrequencyNetwork() {
        return Optional.ofNullable(frequencyNetwork);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        level = tag.getInt("level");
        isValid = !tag.contains("isValid") || tag.getBoolean("isValid");
        if (tag.contains("frequency")) {
            NbtCompound pfIDTag = tag.getCompound("frequency");
            pyramidFrequency = PyramidFrequencyIdentifier.fromNbt(pfIDTag);
            shouldRestoreNetwork = true;
            checkBeamTicks = 82; // Give Beacon some time to catch up
        } else pyramidFrequency = NullFrequencyIdentifier.get();
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("level", level);
        tag.putBoolean("isValid", isValid);
        if (pyramidFrequency.isValid() && pyramidFrequency instanceof PyramidFrequencyIdentifier pfID) {
            NbtCompound pfIDTag = new NbtCompound();
            pfID.toNbt(pfIDTag);
            tag.put("frequency", pfIDTag);
        }
    }

    @Override
    public void serverTick() {
        World world = beaconBlockEntity.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (isValid && --checkBeamTicks <= 0) {
            checkBeamTicks = 5;
            checkBeam();
        }
        if (isValid && world.getTime() % 5L == 0L) {
            Vec3i pos = getBeaconPos();
            Vec3d particlePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).add(vec);
            vec = vec.rotateY(36f * (float) (Math.PI / 180));
            serverWorld.spawnParticles(
                    ParticleTypes.GLOW,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1, 0, 0, 0, 0
            );
        }
        if (isValid && world.getTime() % 80L == 0L) {
            rescanPyramid();
        }
        if (shouldRestoreNetwork) {
            restoreNetwork();
        }
    }

    @Override
    public float[] topColor() {
        var segments = beaconBlockEntity.getBeamSegments();
        if (segments.size() > 0) {
            return segments.get(segments.size()-1).getColor();
        }
        return new float[]{0, 0, 0};
    }
}
