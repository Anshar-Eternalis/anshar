package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
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
    public void setBeacon(BlockPos pos) {
        shouldUpdateBeacon = false;
        beaconPos = pos;
        endCrystal.setBeamTarget(pos.offset(Direction.DOWN, 2));
    }

    @Override
    public boolean onCrystalDamage(DamageSource source) {
        return getConnectedBeacon().map(pos -> {
            World world = endCrystal.getWorld();
            if (!(world instanceof ServerWorld serverWorld)) return true;
            // TODO actually check if the beacon is active as well
            boolean connected = world.getBlockEntity(pos) instanceof BeaconBlockEntity;
            if (connected) {
                if (source.getAttacker() instanceof ServerPlayerEntity serverPlayer && serverPlayer.isSneaking()) {
                    if (!endCrystal.isRemoved()) {
                        endCrystal.dropStack(new ItemStack(Items.END_CRYSTAL));
                        endCrystal.remove(Entity.RemovalReason.KILLED);
                        return true;
                    }
                }
                serverWorld.playSound(
                        null,
                        endCrystal.getX(),
                        endCrystal.getY(),
                        endCrystal.getZ(),
                        SoundEvents.ITEM_TRIDENT_HIT,
                        endCrystal.getSoundCategory(),
                        1f,
                        1f
                );
                double x = endCrystal.getX();
                double y = endCrystal.getY();
                double z = endCrystal.getZ();
                ParticleEffect particleEffect = ParticleTypes.GLOW;
                serverWorld.spawnParticles(particleEffect, x, y+1, z, 8, 0.5, 0.5, 0.5, 4);
            }
            return !connected;
        }).orElse(true);
    }

    @Override
    public void serverTick() {
        tryUpdateBeacon();
        if (!(endCrystal.getWorld() instanceof ServerWorld serverWorld)) return;
        if (serverWorld.getTime() % 10 == 0) {
            double x = endCrystal.getX();
            double y = endCrystal.getY();
            double z = endCrystal.getZ();
            ParticleEffect particleEffect = ParticleTypes.GLOW;
            serverWorld.spawnParticles(particleEffect, x, y+1, z, 1, 0, 1, 0, 5);
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("beaconPos"))
            beaconPos = NbtHelper.toBlockPos(tag.getCompound("beaconPos"));
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (beaconPos != null)
            tag.put("beaconPos", NbtHelper.fromBlockPos(beaconPos));
    }

    @Override
    public Optional<BlockPos> getConnectedBeacon() {
        return Optional.ofNullable(beaconPos);
    }
}
