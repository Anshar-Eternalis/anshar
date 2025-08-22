package com.lgmrszd.anshar.mixin.client.render;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Unique
    private boolean anshar$isInNetwork = false;

    @Inject(method = "render", at = @At("HEAD"))
    public void anshar$render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        Entity entity = camera.getFocusedEntity();
        if (entity instanceof ClientPlayerEntity player) anshar$isInNetwork = PlayerTransportComponent.KEY.get(player).isInNetwork();
        else anshar$isInNetwork = false;
    }

    @WrapWithCondition(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderPass;setRenderer(Ljava/lang/Runnable;)V")
    ) public boolean anshar$setRenderer(RenderPass instance, Runnable runnable) {
        if (anshar$isInNetwork) {
            instance.setRenderer(() -> {
                RenderSystem.clearColor(0, 0, 0, 0.0F);
                RenderSystem.clear(16640);
            });
            return false;
        }
        return true;
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    public void anshar$renderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    public void anshar$renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    public void anshar$renderClouds(FrameGraphBuilder frameGraphBuilder, Matrix4f positionMatrix, Matrix4f projectionMatrix, CloudRenderMode renderMode, Vec3d cameraPos, float ticks, int color, float cloudHeight, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void anshar$renderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
        if (anshar$isInNetwork) ci.cancel();
    }
}
