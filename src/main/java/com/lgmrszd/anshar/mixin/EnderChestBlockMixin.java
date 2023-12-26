package com.lgmrszd.anshar.mixin;

import java.lang.Object;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.storage.EmbeddedStorage;

import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(EnderChestBlock.class)
public abstract class EnderChestBlockMixin {
   private static final Text EMBED_CONTAINER_NAME =
           Text.literal("[")
                   .append(Text.translatable("block.minecraft.beacon"))
                   .append("] ")
                   .append(Text.translatable("container.enderchest"));

   private boolean isBeaconValidStorageTarget(BlockPos pos, World world, BeaconBlockEntity beacon){
      var diff = beacon.getPos().subtract(pos);
      var tier = diff.getY() + 1;
      return tier+1 >= ((BeaconBlockEntityAccessor)(Object)beacon).getLevel() && (
         (tier == Math.abs(diff.getX()) && Math.abs(diff.getZ()) <= tier) || 
         (tier == Math.abs(diff.getZ()) && Math.abs(diff.getX()) <= tier)
      );
      
   }

   @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
   public void Anshar_onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {

      BlockEntity blockEntity = world.getBlockEntity(pos);
      BlockPos blockPos = pos.up();
      if (blockEntity instanceof EnderChestBlockEntity chestEntity
      && !world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {

         var success = world.getLevelProperties().getComponent(NetworkManagerComponent.KEY).getNearestConnectedBeacon(world, pos).map(
            beacon ->  isBeaconValidStorageTarget(pos, world, beacon) && BeaconComponent.KEY.get(beacon).getFrequencyNetwork().map(
               network -> {
                  EmbeddedStorage inventory = network.getStorage();
                  inventory.setActiveBlockEntity(chestEntity);
                  player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, playerx) -> {
                     return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInv, inventory);
                  }, EMBED_CONTAINER_NAME));
                  player.incrementStat(Stats.OPEN_ENDERCHEST);
                  return true;
               }
            ).orElse(false)
         ).orElse(false);

         if (success) {
            ci.setReturnValue(ActionResult.CONSUME);
            ci.cancel();
         }
      } 
   }
}
