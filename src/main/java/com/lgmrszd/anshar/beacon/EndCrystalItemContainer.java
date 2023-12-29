package com.lgmrszd.anshar.beacon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class EndCrystalItemContainer {
    private final ItemStack stack;
    public EndCrystalItemContainer(ItemStack itemStack) {
        stack = itemStack;
    }

    public ActionResult onUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        getBeaconPos().ifPresentOrElse(pos -> clearBeaconPos(), () -> saveBeaconPos(hitResult.getBlockPos()));
        return ActionResult.SUCCESS;
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
