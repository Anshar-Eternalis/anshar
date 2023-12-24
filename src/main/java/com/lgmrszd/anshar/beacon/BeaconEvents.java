package com.lgmrszd.anshar.beacon;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BeaconEvents {
    public final static void register(){
        UseBlockCallback.EVENT.register(BeaconEvents::useBlockEvent);
    }

    private static ActionResult useBlockEvent(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockEntity be = world.getBlockEntity(pos);
        if (
                world.isClient()
                        || !player.isSneaking()
                        || !(be instanceof BeaconBlockEntity bbe)
                        || hand == Hand.OFF_HAND
                        || !player.isHolding(Items.STICK)
        ) return ActionResult.PASS;
        IBeaconComponent freq = IBeaconComponent.KEY.get(bbe);
        if (freq.getFrequencyID().isValid())
            player.sendMessage(Text.literal(
                String.format("Beacon pos: %s, frequency: %s", pos, freq.getFrequencyID().hashCode())
            ));
        else {
            player.sendMessage(Text.literal(
                String.format("Beacon pos: %s, no frequency!", pos)
            ));
        }
        return ActionResult.SUCCESS;
    }
    
}
