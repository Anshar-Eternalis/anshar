package com.lgmrszd.anshar.mixin.accessor;

import net.minecraft.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net/minecraft/block/entity/EnderChestBlockEntity$1")
public interface EnderChestBEVCMAccessor {
    // Hmmm today I will try to access instance of Outer class from within instance of Inner class :clueless:
    @Accessor("field_27218")
    EnderChestBlockEntity getEnderChestBlockEntity();
}
