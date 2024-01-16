package com.lgmrszd.anshar.beacon;

import com.lgmrszd.anshar.config.ServerConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public class BeaconComponentClient {
    static {
        BeaconComponent.clientTick = BeaconComponentClient::clientTick;
    }

    private static int cooldownTicks = 0;

    private static void clientTick (BeaconComponent beaconComponent) {
        if (!ServerConfig.beamClientCheck.get() || cooldownTicks > 0) return;
        Box box = beaconComponent.beamBoundingBox();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.getBoundingBox().intersects(box)) return;
        if (!beaconComponent.isValid()) return;
        LOGGER.debug("Intersecting with beacon beam at {}", beaconComponent.getBeaconPos());
        sendEnterNetworkPacketC2S(beaconComponent.getBeaconPos());
        cooldownTicks = 20;
    }

    public static void clientGlobalTick () {
        if (cooldownTicks > 0) cooldownTicks--;
    }

    private static void sendEnterNetworkPacketC2S(BlockPos pos) {
        var enterPacket = PacketByteBufs.create();
        enterPacket.writeBlockPos(pos);
        ClientPlayNetworking.send(BeaconComponent.ENTER_PACKET_ID, enterPacket);
    }

//    private static void sendEnterNetworkPacketC2S(BlockPos pos, UUID freqUUID) {
//        var enterPacket = PacketByteBufs.create();
//        enterPacket.writeBlockPos(pos);
//        enterPacket.writeUuid(freqUUID);
//        ClientPlayNetworking.send(BeaconComponent.ENTER_PACKET_ID, enterPacket);
//    }

    public static void init() {
    }
}
