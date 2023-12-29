package com.lgmrszd.anshar.mixin;

import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.beacon.IBeaconComponent;
import com.lgmrszd.anshar.beacon.PlayerTransportComponent;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BeaconBlock.class)
public abstract class BeaconBlockMixin extends BlockWithEntity {
    private BeaconBlockMixin(AbstractBlock.Settings settings) {super(settings);}
    
    // insert wrapper override (if not present) to allow for injection
    @Intrinsic
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {        
        super.onSteppedOn(world, pos, state, entity);
    }

    @Inject(method = "onSteppedOn", at = @At("HEAD"))
    public void Anshar_BeaconBlock_onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (world.getTime() % 5L == 0L && entity instanceof PlayerEntity player) {
            world.getBlockEntity(pos, BlockEntityType.BEACON).ifPresent(
                beacon -> IBeaconComponent.KEY.get(beacon).getFrequencyNetwork().ifPresent(
                    network -> PlayerTransportComponent.KEY.get(player).addPlayerToNetwork(network)
                )
            );
        }
   }
}
