package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.Set;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerTransportNetworking {
    public static final Identifier BEACON_TRANSPORT_CHANNEL = new Identifier(MOD_ID, "beacon_transport");

    // public static final <T extends FabricPacket> void receiveNodeListPacketS2C(T packet, ClientPlayerEntity player, PacketSender responseSender) {
    //     PacketByteBuf buf = PacketByteBufs.create();
    //     packet.write(buf);
    //     PlayerTransportComponent.KEY.get(player).setClientJumpCandidates(
    //         buf.readCollection(HashSet<BeaconNode>::new, buffer -> new BeaconNode(buffer.readBlockPos()))
    //     );
    // }

    public static void sendNodeListS2C(ServerPlayerEntity player, Set<BeaconNode> nodes) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCollection(nodes, (buffer, node) -> buffer.writeBlockPos(node.getPos()));
        ServerPlayNetworking.send(player, BEACON_TRANSPORT_CHANNEL, buf);
    }
}
