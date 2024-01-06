package com.lgmrszd.anshar.beacon;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class EndCrystalComponent implements IEndCrystalComponent {
    private BlockPos beaconPos;
    private final EndCrystalEntity endCrystal;
    private boolean linked;
    private Vec3d vec = new Vec3d(1, 0, 0);

    public EndCrystalComponent(EndCrystalEntity endCrystal) {
        this.endCrystal = endCrystal;
        linked = false;
    }


    private EnderChestBlockEntity getChest() {
        LOGGER.info("Interacted! Searching chest... {} {}", endCrystal.getPos(), endCrystal.getBlockPos());
        if (!(endCrystal.getWorld().getBlockEntity(endCrystal.getBlockPos().down())
                instanceof EnderChestBlockEntity enderChestBlockEntity))
            return null;
        return enderChestBlockEntity;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand) {
        EnderChestBlockEntity enderChestBlockEntity = getChest();
        if (enderChestBlockEntity == null)
            return ActionResult.PASS;
        World world = endCrystal.getWorld();
//        if (world instanceof ServerWorld serverWorld)) return ActionResult.SUCCESS;
        BlockPos pos = enderChestBlockEntity.getPos();
        return world.getBlockState(pos).onUse(
                world,
                player,
                hand,
                new BlockHitResult(
                        new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5),
                        Direction.UP,
                        pos,
                        false
                )
        );
    }

    @Override
    public void setBeacon(BlockPos pos) {
        beaconPos = pos;
        endCrystal.setBeamTarget(pos.offset(Direction.DOWN, 2));
        linked = true;
    }

    public void clearBeacon() {
        beaconPos = null;
        endCrystal.setBeamTarget(null);
        linked = false;
    }

    @Override
    public Vec3d getPos() {
        return endCrystal.getPos();
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
        if (!linked) return;
        if (!(endCrystal.getWorld() instanceof ServerWorld serverWorld)) return;
        if (serverWorld.getTime() % 5 == 0) {
            if (!(serverWorld.getBlockEntity(beaconPos) instanceof BeaconBlockEntity)) {
                clearBeacon();
            }
        }
        if (serverWorld.getTime() % 5 == 0) {

            Vec3d particlePos = endCrystal.getPos().add(vec).add(0, 0.7, 0);
            vec = vec.rotateY(36f * (float) (Math.PI / 180));

            ParticleEffect particleEffect = ParticleTypes.GLOW;
            serverWorld.spawnParticles(
                    particleEffect,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1, 0, 0, 0, 0
            );
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        linked = tag.getBoolean("linked");
        if (tag.contains("beaconPos"))
            beaconPos = NbtHelper.toBlockPos(tag.getCompound("beaconPos"));
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("linked", linked);
        if (beaconPos != null)
            tag.put("beaconPos", NbtHelper.fromBlockPos(beaconPos));
    }

    @Override
    public Optional<BlockPos> getConnectedBeacon() {
        return Optional.ofNullable(beaconPos);
    }
}
