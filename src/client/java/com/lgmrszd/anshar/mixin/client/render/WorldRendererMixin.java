package com.lgmrszd.anshar.mixin.client.render;

import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    private boolean anshar$isInNetwork = false;

    @Inject(method = "render", at = @At("HEAD"))
    public void anshar$render(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        Entity entity = camera.getFocusedEntity();
        if (entity instanceof ClientPlayerEntity player) anshar$isInNetwork = PlayerTransportComponent.KEY.get(player).isInNetwork();
        else anshar$isInNetwork = false;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V"))
    public void anshar$backgroundRenderOverride(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness) {
        if (!anshar$isInNetwork) BackgroundRenderer.render(camera, tickDelta, world, viewDistance, skyDarkness);
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void anshar$renderSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void anshar$renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    public void anshar$renderClouds(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void anshar$renderWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderWorldBorder", at = @At("HEAD"), cancellable = true)
    public void anshar$renderWorldBorder(Camera camera, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }
    
}
