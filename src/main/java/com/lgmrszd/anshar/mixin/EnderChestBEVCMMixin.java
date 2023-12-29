package com.lgmrszd.anshar.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.lgmrszd.anshar.Anshar.LOGGER;

@Mixin(targets = "net/minecraft/block/entity/EnderChestBlockEntity$1")
public class EnderChestBEVCMMixin {
    @Inject(at = @At("HEAD"), method = "isPlayerViewing", cancellable = true)
    public void injected(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player.currentScreenHandler instanceof GenericContainerScreenHandler gcsh) {
            Inventory inventory = gcsh.getInventory();
        }
    }
}
