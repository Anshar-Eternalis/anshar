package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.Anshar;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Optional;

public class EndCrystalItemContainer {
    private final ItemStack stack;
    public EndCrystalItemContainer(ItemStack itemStack) {
        stack = itemStack;
    }

    public ActionResult onUse(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.PASS;
        World world = context.getWorld();
        BlockPos targetPos = context.getBlockPos();
        // Case 1: Binding End Crystal to a beacon
        if (player.isSneaking() && world.getBlockState(targetPos).isOf(Blocks.BEACON)) {
            if (world.isClient()) return ActionResult.SUCCESS;
            boolean samePos = getBeaconPos().map(pos -> pos.equals(targetPos)).orElse(false);
            if (samePos) {
                clearBeaconPos();
                player.sendMessage(Text.literal("Cleared Beacon position"));
            } else {
                saveBeaconPos(targetPos);
                player.sendMessage(Text.literal("Saved Beacon position"));
            }
            return ActionResult.SUCCESS;
        }
        // Case 2: placing on top of an Ender Chest
        // VanillaCopy: EndCrystalItem
        if (player.isSneaking() && world.getBlockState(targetPos).isOf(Blocks.ENDER_CHEST)) {
            BlockPos up = targetPos.up();
            if (!world.isAir(up)) return ActionResult.PASS;

            double x = up.getX();
            double y = up.getY();
            double z = up.getZ();
            List<Entity> list = world.getOtherEntities(null, new Box(x, y, z, x + 1.0, y + 2.0, z + 1.0));
            if (!list.isEmpty()) return ActionResult.FAIL;
            if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;
            int maxDistance = world.getGameRules().getInt(Anshar.END_CRYSTAL_LINKING_DISTANCE);


            Optional<BlockPos> beaconPos = getBeaconPos()
                    .or(() -> NetworkManagerComponent.KEY.get(world.getLevelProperties())
                            .getNearestConnectedBeacon(world, up)
                            .map(BlockEntity::getPos)
                    )
                    .filter(pos -> pos.isWithinDistance(up, maxDistance));


            return beaconPos.map(pos -> {
                EndCrystalEntity endCrystalEntity = new EndCrystalEntity(world, x + 0.5, y, z + 0.5);
                endCrystalEntity.setShowBottom(false);
                world.spawnEntity(endCrystalEntity);
                IEndCrystalComponent component = EndCrystalComponent.KEY.get(endCrystalEntity);
                component.setBeacon(pos);
                world.emitGameEvent(player, GameEvent.ENTITY_PLACE, up);
                context.getStack().decrement(1);
                return ActionResult.SUCCESS;
            }).orElseGet(() -> {
                serverWorld.playSound(
                        null,
                        x, y, z,
                        SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
                        SoundCategory.BLOCKS,
                        1f,
                        0.5f
                );
                return ActionResult.FAIL;
            });
        }

        return ActionResult.PASS;
    }

    private void saveBeaconPos(BlockPos pos) {
        NbtCompound tag = stack.getOrCreateNbt();
        NbtCompound posTag = NbtHelper.fromBlockPos(pos);
        tag.put("BeaconPos", posTag);
        stack.setNbt(tag);
    }

    private void clearBeaconPos() {
        NbtCompound tag = stack.getNbt();
        if (tag == null) return;
        tag.remove("BeaconPos");
        if (tag.isEmpty()) stack.setNbt(null);
        else stack.setNbt(tag);
    }

    public Optional<BlockPos> getBeaconPos() {
        if (!stack.hasNbt()) return Optional.empty();
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("BeaconPos")) return Optional.empty();
        return Optional.of(NbtHelper.toBlockPos(tag.getCompound("BeaconPos")));
    }
}
