package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.storage.EmbeddedStorage;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(EnderChestBlockEntity.class)
public class EnderChestBlockEntityMixin implements Inventory {

    @Unique
    private <T> T anshar$callOnEmbeddedStorage(Function<EmbeddedStorage, T> function, T defaultValue) {
        return EmbeddedStorage.getForEnderChestBlockEntity((EnderChestBlockEntity)(Object)this)
                .map(function)
                .orElse(defaultValue);
    }

    @Unique
    private <T> void anshar$callOnEmbeddedStorage(Consumer<EmbeddedStorage> function) {
        EmbeddedStorage.getForEnderChestBlockEntity((EnderChestBlockEntity)(Object)this)
                .ifPresent(function);
    }

    @Override
    public int size() {
        return anshar$callOnEmbeddedStorage(SimpleInventory::size, 0);
//        return 0;
    }

    @Override
    public boolean isEmpty() {
        return anshar$callOnEmbeddedStorage(SimpleInventory::isEmpty, false);
//        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.getStack(slot), ItemStack.EMPTY);
//        return null;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.removeStack(slot, amount), ItemStack.EMPTY);
//        return null;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.removeStack(slot), ItemStack.EMPTY);
//        return null;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.setStack(slot, stack));
    }

    @Override
    public void markDirty() {
        anshar$callOnEmbeddedStorage(SimpleInventory::markDirty);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
//        return ((EnderChestBlockEntity)(Object)this).canPlayerUse(player);
        return anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.canPlayerUse(player), false);
//        return false;
    }

    @Inject(method = "canPlayerUse", at = @At("HEAD"), cancellable = true)
    private void ansharCanPlayerUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        // canPlayerUse already exists in ECBE
        cir.setReturnValue(
                anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.canPlayerUse(player), false)
        );
    }

    @Override
    public void clear() {
        anshar$callOnEmbeddedStorage(SimpleInventory::clear);
    }
}
