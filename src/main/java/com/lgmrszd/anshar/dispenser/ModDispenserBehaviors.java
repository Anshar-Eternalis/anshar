package com.lgmrszd.anshar.dispenser;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.IBeaconComponent;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;

public class ModDispenserBehaviors {
    public static void register() {
//        oldFireworkBehavior = DispenserBlock.getB
//        DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new ItemDispenserBehavior() {
//            @Override
//            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
//                return super.dispenseSilently(pointer, stack);
//            }
//        });
        DispenserBlock.registerBehavior(Items.AMETHYST_SHARD, (pointer, stack) -> {
            BlockPos facingPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
            // TODO IDEA gives me weird warning, what??
            ServerWorld world = pointer.world();
            if (world.getBlockEntity(facingPos) instanceof BeaconBlockEntity bbe) {
                IBeaconComponent beaconComponent = BeaconComponent.KEY.get(bbe);
                beaconComponent.activate();
            }
            return stack;
        });
    }
}
