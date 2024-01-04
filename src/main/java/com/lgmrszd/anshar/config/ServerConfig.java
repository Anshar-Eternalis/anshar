package com.lgmrszd.anshar.config;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class ServerConfig {
    public static final Identifier END_CRYSTAL_MAX_DISTANCE_ID =
            new Identifier(MOD_ID, "config_end_crystal_max_distance");
    public static void sendNewValue(ServerPlayerEntity serverPlayer, int value) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(value);
        ServerPlayNetworking.send(
                serverPlayer,
                END_CRYSTAL_MAX_DISTANCE_ID,
                buf
        );
    }
}
