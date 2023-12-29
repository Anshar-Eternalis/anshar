package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.ModApi;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class EndCrystalEvents {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack heldStack = player.getMainHandStack();
            if (!(
                    player.isHolding(Items.END_CRYSTAL) &&
                            player.isSneaking() &&
                            hand == Hand.MAIN_HAND &&
                            world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.BEACON &&
                            player.getMainHandStack().getCount() == 1
            )) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.SUCCESS;
            EndCrystalItemContainer container = ModApi.END_CRYSTAL_ITEM.find(heldStack, null);
            if (container == null) return ActionResult.FAIL;
            return container.onUse(player, world, hand, hitResult);
        });
    }
}
