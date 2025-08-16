package com.lgmrszd.anshar.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Final
    @Shadow MinecraftClient client;

    @Unique
    private boolean anshar$networkMode = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void anshar$render(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        anshar$networkMode = client.getCameraEntity() instanceof ClientPlayerEntity player && PlayerTransportComponent.KEY.get(player).isInNetwork();
    }

    @ModifyExpressionValue(
        method = "renderWorld", 
        at = @At(
            value = "FIELD", 
            target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", 
            opcode = Opcodes.GETFIELD 
    ))
    private boolean proxyRenderHand(boolean original) {
        return original && !anshar$networkMode;
    }
}
