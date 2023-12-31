package com.lgmrszd.anshar.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.beacon.PlayerTransportComponent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void anshar$render(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (PlayerTransportComponent.KEY.get(client.player).isInNetwork()) ci.cancel();
    }
}
