package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.PlayerTransportComponent;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;


public class PlayerTransportClient {
    private static final int TICKS_TO_JUMP = 20 * 5;
    private static int gateTicks = 0;

    public static void tick(ClientWorld world) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        var transport = PlayerTransportComponent.KEY.get(player);
        if (transport.isInNetwork()) {
            
            if (player.input.pressingForward) {
                gateTicks += 1;
                System.out.println(gateTicks);
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
}
