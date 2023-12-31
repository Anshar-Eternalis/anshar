package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.PlayerTransportComponent;
import com.lgmrszd.anshar.beacon.TransportEffects;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;

public class PlayerTransportClient {
    private static final int TICKS_TO_JUMP = 20 * 3;
    private static int gateTicks = 0;

    public static void tick(ClientWorld world) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        var transport = PlayerTransportComponent.KEY.get(player);
        if (transport.isInNetwork()) {
            
            if (player.input.pressingForward) {
                gateTicks += 1;
                if (gateTicks >= TICKS_TO_JUMP) {
                    gateTicks = 0;
                    ClientPlayNetworking.send(PlayerTransportComponent.JUMP_PACKET_ID, PacketByteBufs.empty());
                }
            } else {
                gateTicks = 0;
            }
        } else {
            gateTicks = 0;
        }
    }

    public static float getJumpPercentage() { return (float)gateTicks / (float)TICKS_TO_JUMP; }

    public static void acceptExplosionPacketS2C(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var pos = buf.readBlockPos().toCenterPos();
        client.execute(() -> handler.getWorld().addFireworkParticle(pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, TransportEffects.TRANSPORT_EXPLOSION_FIREWORK));
    }
}
