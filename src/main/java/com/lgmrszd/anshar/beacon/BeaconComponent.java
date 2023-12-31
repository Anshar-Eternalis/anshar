package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.frequency.*;
import com.lgmrszd.anshar.mixin.accessor.BeaconBlockEntityAccessor;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class BeaconComponent implements IBeaconComponent {
    private final BeaconBlockEntity beaconBlockEntity;

    private IFrequencyIdentifier pyramidFrequency;

    private FrequencyNetwork frequencyNetwork;
    private boolean shouldRestoreFrequencyNetwork;
    private int level;

    public BeaconComponent(BeaconBlockEntity beaconBlockEntity) {
        this.beaconBlockEntity = beaconBlockEntity;
        level = 0;
        pyramidFrequency = NullFrequencyIdentifier.get();
        shouldRestoreFrequencyNetwork = true;
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

//        IFrequencyIdentifier newFreqID = PyramidFrequencyIdentifier.scanForPyramid(world, getBeaconPos(), level);
        if (!pyramidFrequency.equals(newFreqID)) onFrequencyIDUpdate(pyramidFrequency, newFreqID);
    }

    private void onFrequencyIDUpdate(IFrequencyIdentifier oldFreqID, IFrequencyIdentifier newFreqID) {
        World world = beaconBlockEntity.getWorld();
        if (world == null) {
            return;
        }

//        getFreqComponent().set(newFreqID);
        pyramidFrequency = newFreqID;

        shouldRestoreFrequencyNetwork = true;
        beaconBlockEntity.getWorld().getPlayers().forEach(playerEntity -> {
            playerEntity.sendMessage(Text.of(
                    String.format("Frequency updated!!\nOld: %s\nNew: %s", oldFreqID, newFreqID))
            );
        });
    }

    private void tryUpdateFrequencyNetwork() {
        if (!shouldRestoreFrequencyNetwork) return;
        World world = beaconBlockEntity.getWorld();
        if (world == null) {
            return;
        }
        NetworkManagerComponent networkManagerComponent = NetworkManagerComponent.KEY.get(world.getLevelProperties());
        networkManagerComponent.updateBeaconNetwork(this, pyramidFrequency, frequencyNetwork1 -> {
            frequencyNetwork = frequencyNetwork1;
        });
        shouldRestoreFrequencyNetwork = false;
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
    public void activate() {
        LOGGER.info("Attempted to activate!");
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
        if (tag.contains("frequency")) {
            NbtCompound pfIDTag = tag.getCompound("frequency");
            pyramidFrequency = PyramidFrequencyIdentifier.fromNbt(pfIDTag);
        } else pyramidFrequency = NullFrequencyIdentifier.get();
        shouldRestoreFrequencyNetwork = true;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("level", level);
        if (pyramidFrequency.isValid() && pyramidFrequency instanceof PyramidFrequencyIdentifier pfID) {
            NbtCompound pfIDTag = new NbtCompound();
            pfID.toNbt(pfIDTag);
            tag.put("frequency", pfIDTag);
        }
    }

    @Override
    public void serverTick() {
        World world = beaconBlockEntity.getWorld();
        if (world == null) return;
        if (world.getTime() % 80L == 0L) {
            rescanPyramid();
        }
        tryUpdateFrequencyNetwork();
    }
}
