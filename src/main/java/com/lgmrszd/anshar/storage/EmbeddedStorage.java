package com.lgmrszd.anshar.storage;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.EndCrystalComponent;
import com.lgmrszd.anshar.beacon.IEndCrystalComponent;
import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.mixin.accessor.BeaconBlockEntityAccessor;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;


public class EmbeddedStorage extends EnderChestInventory {
    private static final int CONNECTION_RADIUS = 20;
    
    private static boolean isBeaconValidStorageTarget(BlockPos pos, World world, BeaconBlockEntity beacon){
        var diff = beacon.getPos().subtract(pos);
        var tier = diff.getY() + 1;
        return tier <= ((BeaconBlockEntityAccessor)beacon).getLevel() && (
                (tier == Math.abs(diff.getX()) && Math.abs(diff.getZ()) <= tier) ||
                        (tier == Math.abs(diff.getZ()) && Math.abs(diff.getX()) <= tier)
        );
    }

    public static Optional<BeaconBlockEntity> getConnectedBeacon(World world, BlockPos pos, EnderChestBlockEntity blockEnt) {
        // check for crystal
        BlockPos blockPos2 = pos.up();
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        List<Entity> list = world.getOtherEntities(null, new Box(d, e, f, d + 1.0, e + 2.0, f + 1.0));
        if (!list.isEmpty()) {
            for (Entity entity: list) {
                if (!(entity instanceof EndCrystalEntity ece)) continue;
                IEndCrystalComponent ecc = EndCrystalComponent.KEY.get(ece);
                Optional<BeaconBlockEntity> lookUpFromCrystal = ecc.getConnectedBeacon().map(pos1 -> {
                    if (world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe) return bbe;
                    return null;
                });
                if (lookUpFromCrystal.isPresent()) return lookUpFromCrystal;
            }
        }
        // if no crystal, check for pyramid (closer the better)
        for (BeaconBlockEntity beacon : NetworkManagerComponent.KEY.get(world.getLevelProperties()).getConnectedBeaconsInRadius(world, pos, CONNECTION_RADIUS * 1.0)) {
            if (isBeaconValidStorageTarget(pos, world, beacon)) return Optional.of(beacon);
        }
        return Optional.empty();
    }

    public static Optional<EmbeddedStorage> getForEnderChestBlockEntity(EnderChestBlockEntity ECBE) {
        if (ECBE.getWorld() == null) return Optional.empty();
        return EmbeddedStorage.getConnectedBeacon(ECBE.getWorld(), ECBE.getPos(), ECBE)
                .flatMap(beacon -> BeaconComponent.KEY.get(beacon)
                .getFrequencyNetwork()
                .map(FrequencyNetwork::getStorage));
    }

    public Text getContainerLabelFor(BeaconBlockEntity beacon){
        return Text.literal("[")
            .append(beacon.getName())
            .append("] ")
            .append(Text.translatable("container.enderchest"));
    }
}
