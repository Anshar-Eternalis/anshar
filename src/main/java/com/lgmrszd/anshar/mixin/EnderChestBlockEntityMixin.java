package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.storage.EmbeddedStorage;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(EnderChestBlockEntity.class)
@Implements(@Interface(iface = Inventory.class, prefix = "ansharinv$"))
public abstract class EnderChestBlockEntityMixin {

    @Shadow public abstract boolean shadow$canPlayerUse(PlayerEntity player);

    @Unique
    private <T> T anshar$callOnEmbeddedStorage(Function<EmbeddedStorage, T> function, T defaultValue) {
        return EmbeddedStorage.getForEnderChestBlockEntity((EnderChestBlockEntity)(Object)this)
                .map(function)
                .orElse(defaultValue);
    }

    @Unique
    private void anshar$callOnEmbeddedStorage(Consumer<EmbeddedStorage> function) {
        EmbeddedStorage.getForEnderChestBlockEntity((EnderChestBlockEntity)(Object)this)
                .ifPresent(function);
    }

    public int ansharinv$size() {
        return anshar$callOnEmbeddedStorage(SimpleInventory::size, 0);
    }

    public boolean ansharinv$isEmpty() {
        return anshar$callOnEmbeddedStorage(SimpleInventory::isEmpty, false);
    }

    public ItemStack ansharinv$getStack(int slot) {
        return anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.getStack(slot), ItemStack.EMPTY);
    }

    public ItemStack ansharinv$removeStack(int slot, int amount) {
        return anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.removeStack(slot, amount), ItemStack.EMPTY);
    }

    public ItemStack ansharinv$removeStack(int slot) {
        return anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.removeStack(slot), ItemStack.EMPTY);
    }

    public void ansharinv$setStack(int slot, ItemStack stack) {
        anshar$callOnEmbeddedStorage(embeddedStorage -> embeddedStorage.setStack(slot, stack));
    }

    public void ansharinv$markDirty() {
        anshar$callOnEmbeddedStorage(SimpleInventory::markDirty);
    }

    @Intrinsic(displace = true)
    public boolean ansharinv$canPlayerUse(PlayerEntity player) {
        return EmbeddedStorage.getForEnderChestBlockEntity((EnderChestBlockEntity)(Object)this)
                .map(embeddedStorage -> embeddedStorage.canPlayerUse(player))
                .orElse(this.shadow$canPlayerUse(player));
    }


    public void ansharinv$clear() {
        anshar$callOnEmbeddedStorage(SimpleInventory::clear);
    }
}
