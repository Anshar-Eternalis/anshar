package com.lgmrszd.anshar.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lgmrszd.anshar.PlayerTransportClient;
import com.lgmrszd.anshar.beacon.PlayerTransportComponent;

import net.minecraft.client.network.AbstractClientPlayerEntity;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @Inject(method = "getFovMultiplier", at = @At("HEAD"), cancellable = true)
    public void getFovMultiplier(CallbackInfoReturnable<Float> ci) {
        var transport = PlayerTransportComponent.KEY.get(this);
        if (transport != null && transport.isInNetwork()) {
            ci.setReturnValue(1.0f + PlayerTransportClient.getJumpPercentage() * 0.5f);
            ci.cancel();
        }
    }
}
