package com.lgmrszd.anshar;

import com.lgmrszd.anshar.freq.IBeaconComponent;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class MyEvents {
    public static void register() {
        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockEntity be = world.getBlockEntity(pos);
            if (
                    world.isClient()
                            || !player.isSneaking()
                            || !(be instanceof BeaconBlockEntity bbe)
                            || hand == Hand.OFF_HAND
                            || !player.isHolding(Items.STICK)
            ) return ActionResult.PASS;
            IBeaconComponent freq = MyComponents.BEACON.get(bbe);
            if (freq.getFrequency() == null)
                player.sendMessage(Text.literal(String.format("Beacon pos: %s, no frequency!",
                        pos))
                );
            else
                player.sendMessage(Text.literal(String.format("Beacon pos: %s, frequency: %s",
                        pos,
                        freq.getFrequency().hashCode()))
                );
            return ActionResult.SUCCESS;
        }));
    }
}
