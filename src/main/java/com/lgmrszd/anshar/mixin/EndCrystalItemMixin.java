package com.lgmrszd.anshar.mixin;

import com.lgmrszd.anshar.ModApi;
import com.lgmrszd.anshar.beacon.EndCrystalItemContainer;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

@Mixin(EndCrystalItem.class)
public abstract class EndCrystalItemMixin extends Item {

    public EndCrystalItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void anshar$useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
        EndCrystalItemContainer container = ModApi.END_CRYSTAL_ITEM.find(context.getStack(), null);
        if (container == null) return;
        ActionResult result = container.onUse(context);
        if (result != ActionResult.PASS) {
            ci.setReturnValue(result);
        }
    }
}
