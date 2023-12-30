package com.lgmrszd.anshar.mixin;

import java.lang.Object;
import java.util.List;
import java.util.Optional;

import com.lgmrszd.anshar.beacon.EndCrystalComponent;
import com.lgmrszd.anshar.beacon.IEndCrystalComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.mixin.accessor.BeaconBlockEntityAccessor;
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

import static com.lgmrszd.anshar.Anshar.LOGGER;

@Mixin(EnderChestBlock.class)
public abstract class EnderChestBlockMixin {
   private static int CONNECTION_RADIUS = 20;
   private static final Text EMBED_CONTAINER_NAME =
           Text.literal("[")
                   .append(Text.translatable("block.minecraft.beacon"))
                   .append("] ")
                   .append(Text.translatable("container.enderchest"));

   /*
    * Two modes:
    * - if on a networked pyramid: override default, connect to network
    * - if connected to a networked crystal: override above, connect to crystal network
    */

   private boolean isBeaconValidStorageTarget(BlockPos pos, World world, BeaconBlockEntity beacon){
      var diff = beacon.getPos().subtract(pos);
      var tier = diff.getY() + 1;
      return tier <= ((BeaconBlockEntityAccessor)(Object)beacon).getLevel() && (
         (tier == Math.abs(diff.getX()) && Math.abs(diff.getZ()) <= tier) || 
         (tier == Math.abs(diff.getZ()) && Math.abs(diff.getX()) <= tier)
      );
   }

   private Optional<BeaconBlockEntity> getConnectedBeacon(World world, BlockPos pos, EnderChestBlockEntity blockEnt) {
      // check for crystal
      // ...
      BlockPos blockPos2 = pos.up();
      double d = blockPos2.getX();
      double e = blockPos2.getY();
      double f = blockPos2.getZ();
      List<Entity> list = world.getOtherEntities(null, new Box(d, e, f, d + 1.0, e + 2.0, f + 1.0));
      if (list.isEmpty()) LOGGER.info("No entities detected");
      else {
         for (Entity entity: list) {
            if (!(entity instanceof EndCrystalEntity ece)) continue;
            IEndCrystalComponent ecc = EndCrystalComponent.KEY.get(ece);
            Optional<BeaconBlockEntity> lookUpFromCrystal = ecc.getConnectedBeacon().map(pos1 -> {
               if (world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe) return bbe;
               return null;
            });
            if (lookUpFromCrystal.isPresent()) return lookUpFromCrystal;
         }
      }
      // if no crystal, check for pyramid (closer the better)
      for (BeaconBlockEntity beacon : NetworkManagerComponent.KEY.get(world.getLevelProperties()).getConnectedBeaconsInRadius(world, pos, CONNECTION_RADIUS * 1.0)) {
         if (isBeaconValidStorageTarget(pos, world, beacon)) return Optional.of(beacon);
      }
      System.out.println("no valid beacons found");
      return Optional.empty();
   }

   @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
   public void anshar$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {

      if (world.isClient) return;

      BlockEntity blockEntity = world.getBlockEntity(pos);
      BlockPos topBlockPos = pos.up();
      if (blockEntity instanceof EnderChestBlockEntity chestEntity
      && !world.getBlockState(topBlockPos).isSolidBlock(world, pos)) {
         var success = getConnectedBeacon(world, pos, chestEntity).map(
            beacon ->  BeaconComponent.KEY.get(beacon).getFrequencyNetwork().map(
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
