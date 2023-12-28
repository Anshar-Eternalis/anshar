package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.lgmrszd.anshar.Anshar.LOGGER;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
    @Inject(at = @At("HEAD"), method = "onStateReplaced")
    public void anshar_onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (state.getBlock().equals(Blocks.BEACON) && !newState.getBlock().equals(Blocks.BEACON)) {
            LOGGER.debug("Detected Beacon Block removed...");
            if (!(world.getBlockEntity(pos) instanceof BeaconBlockEntity bbe)) return;
            // TODO: rewrite this with new NetworkManagerComponent code. Maybe force the component to update with invalid frequency?
            BeaconComponent.KEY.get(bbe).getFrequencyNetwork().ifPresent(frequencyNetwork -> {
                frequencyNetwork.removeBeacon(bbe.getPos());
            });
        }
    }
}
