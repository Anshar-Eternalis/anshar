package com.lgmrszd.anshar.debug;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugClient {

    protected static final ConcurrentHashMap<DebugLine, Long> DEBUG_LINES = new ConcurrentHashMap<>();


    public static void init() {
        ClientTickEvents.START_WORLD_TICK.register(DebugClient::onStartTick);
        ClientPlayNetworking.registerGlobalReceiver(DebugCommon.DEBUG_LINE_ID, DebugClient::receiveDebugLinePacket);
    }

    private static void onStartTick(ClientWorld clientWorld) {
        long currentTime = clientWorld.getTime();
        for (Map.Entry<DebugLine, Long> entry : DEBUG_LINES.entrySet()) {
            if (entry.getValue() < currentTime) {
                DEBUG_LINES.remove(entry.getKey());
            }
        }
    }

    public static void addDebugLine(DebugLine line, long lifetime) {
        DEBUG_LINES.put(line, lifetime);
    }

    private static void receiveDebugLinePacket(
            MinecraftClient client,
            ClientPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        int linesToStore = buf.readInt();
        while (linesToStore-- > 0) {
            BlockPos start = buf.readBlockPos();
            BlockPos end = buf.readBlockPos();
            int colorStart = buf.readInt();
            int colorEnd = buf.readInt();
            long lifetime = buf.readLong();
            addDebugLine(new DebugLine(start, end, colorStart, colorEnd), lifetime);
        }
    }
}
