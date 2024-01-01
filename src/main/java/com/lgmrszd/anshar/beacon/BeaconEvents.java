package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.ModApi;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class BeaconEvents {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack heldStack = player.getMainHandStack();
            if (!(
                    player.isSneaking() &&
                            hand == Hand.MAIN_HAND &&
                            world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.BEACON
            )) return ActionResult.PASS;
            if (player.isHolding(Items.END_CRYSTAL) && player.getMainHandStack().getCount() == 1) {
                if (world.isClient()) return ActionResult.SUCCESS;
                EndCrystalItemContainer container = ModApi.END_CRYSTAL_ITEM.find(heldStack, null);
                if (container == null) return ActionResult.FAIL;
                return container.onUse(player, world, hand, hitResult);
            }
//            if (player.isHolding(Items.AMETHYST_SHARD)) {
//                if (world.isClient()) return ActionResult.SUCCESS;
//                if (!(world.getBlockEntity(hitResult.getBlockPos()) instanceof BeaconBlockEntity bbe)) return ActionResult.PASS;
//                BeaconComponent.KEY.get(bbe).activate();
//            }
            return ActionResult.PASS;
        });
    }
}
