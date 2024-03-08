package com.lgmrszd.anshar.debug;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class DebugCommon {
    public static final Identifier DEBUG_LINE_ID = new Identifier(MOD_ID, "debug_line");
    private static final ConcurrentHashMap<UUID, LinkedBlockingQueue<DebugLine>> DEBUG_LINES = new ConcurrentHashMap<>();

    private static final int LINES_PER_PACKET = 32;


    public static void addDebugLine(DebugLine line, ServerWorld world) {
        world.getPlayers().stream()
                .filter(playerEntity ->
                        playerEntity.getBlockPos().isWithinDistance(line.start(), 32) &&
                                playerEntity.isHolding(Items.DEBUG_STICK)
                )
                .forEach(playerEntity -> {
                    DEBUG_LINES.computeIfAbsent(playerEntity.getUuid(), uuid -> new LinkedBlockingQueue<>()).add(line);
                });
//        DEBUG_LINES.add(line);
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(DebugCommon::onEndTick);
    }

    private static void onEndTick(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            UUID playerUuid = player.getUuid();
            LinkedBlockingQueue<DebugLine> queue = DEBUG_LINES.get(playerUuid);
            if (queue != null) onEndTick(player, queue);
        });
    }

    private static void onEndTick(ServerPlayerEntity player, LinkedBlockingQueue<DebugLine> queue) {
        if (queue.isEmpty()) return;
        int i = 0;
        int linesToSend = Math.min(LINES_PER_PACKET, queue.size());
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(linesToSend);
        DebugLine currentLine;
        currentLine = queue.poll();
        while (i++ < LINES_PER_PACKET && currentLine != null) {
            buf.writeBlockPos(currentLine.start());
            buf.writeBlockPos(currentLine.end());
            buf.writeLong(currentLine.lifetime());

            currentLine = queue.poll();
        }
        ServerPlayNetworking.send(player, DEBUG_LINE_ID, buf);
//        }
    }
}
