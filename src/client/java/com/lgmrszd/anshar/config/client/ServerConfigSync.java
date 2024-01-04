package com.lgmrszd.anshar.config.client;

import com.lgmrszd.anshar.config.ServerConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class ServerConfigSync {
    private static int END_CRYSTAL_MAX_DISTANCE;

    private static void setEndCrystalMaxDistance(int value) {
        END_CRYSTAL_MAX_DISTANCE = value;
    }

    public static int getEndCrystalMaxDistance() {
        return END_CRYSTAL_MAX_DISTANCE;
    }

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
                ServerConfig.END_CRYSTAL_MAX_DISTANCE_ID,
                ServerConfigSync::acceptConfigUpdateS2C
        );
    }

    private static void acceptConfigUpdateS2C(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int endCrystalMaxDistance = buf.readInt();
        client.execute(() -> {
            setEndCrystalMaxDistance(endCrystalMaxDistance);
        });
    }
}
