package com.lgmrszd.anshar.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportGateParticle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Unique
    private boolean anshar$filterParticles = false;

    @Inject(method = "renderParticles", at = @At("HEAD"))
    public void renderParticles(LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta, CallbackInfo ci) {
        anshar$filterParticles = PlayerTransportComponent.KEY.get(MinecraftClient.getInstance().player).isInNetwork();
    }

    @WrapOperation(method = "renderParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"))
    public void proxyBuildGeometry(Particle particle, VertexConsumer vertexConsumer, Camera camera, float tickDelta, Operation<Void> original) {
        if (anshar$filterParticles && !(particle instanceof TransportGateParticle)) return;
        original.call(particle, vertexConsumer, camera, tickDelta);
    } 
}
