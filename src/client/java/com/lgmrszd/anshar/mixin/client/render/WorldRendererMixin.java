package com.lgmrszd.anshar.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Unique private boolean anshar$isInNetwork = false;

    @Inject(method = "render", at = @At("HEAD"))
    public void anshar$render(CallbackInfo ci, @Local(argsOnly = true) Camera camera) {
        Entity entity = camera.getFocusedEntity();
        if (entity instanceof ClientPlayerEntity player) anshar$isInNetwork = PlayerTransportComponent.KEY.get(player).isInNetwork();
        else anshar$isInNetwork = false;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V"))
    public void anshar$backgroundRenderOverride(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness) {
        if (!anshar$isInNetwork) BackgroundRenderer.render(camera, tickDelta, world, viewDistance, skyDarkness);
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void anshar$renderSky(CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void anshar$renderEntity(CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    public void anshar$renderClouds(CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void anshar$renderWeather(CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderWorldBorder", at = @At("HEAD"), cancellable = true)
    public void anshar$renderWorldBorder(CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }
    
}
