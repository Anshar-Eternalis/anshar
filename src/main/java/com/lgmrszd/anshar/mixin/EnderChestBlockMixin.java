package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.storage.EnderChestComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(EnderChestBlock.class)
public abstract class EnderChestBlockMixin {

   /*
    * Two modes:
    * - if on a networked pyramid: override default, connect to network
    * - if connected to a networked crystal: override above, connect to crystal network
    */

   @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
   public void anshar$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {

      if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return;

      BlockEntity blockEntity = world.getBlockEntity(pos);
      BlockPos topBlockPos = pos.up();
      if (blockEntity instanceof EnderChestBlockEntity chestEntity
      && !world.getBlockState(topBlockPos).isSolidBlock(world, pos)) {

         boolean success = EnderChestComponent.KEY.get(chestEntity).tryOpen(serverPlayer);

         if (success) {
            ci.setReturnValue(ActionResult.CONSUME);
            ci.cancel();
         }
      } 
   }
}
