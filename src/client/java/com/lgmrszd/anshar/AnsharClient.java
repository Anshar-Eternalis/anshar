package com.lgmrszd.anshar;

import java.util.HashSet;
import java.util.Set;

import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.beacon.PlayerTransportComponent;
import com.lgmrszd.anshar.beacon.PlayerTransportNetworking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import net.minecraft.client.MinecraftClient;

public class AnsharClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(PlayerTransportNetworking.BEACON_TRANSPORT_CHANNEL, AnsharClient::receiveNodeListPacketS2C);
	}

	public static final void receiveNodeListPacketS2C(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        setClientJumpCandidates(
            buf.readCollection(HashSet<BeaconNode>::new, buffer -> new BeaconNode(buffer.readBlockPos()))
        );
	}

	private static void setClientJumpCandidates(Set<BeaconNode> nodes) {
        System.out.println("recv'd beacon nodes packet. candidates:");
        for (var node : nodes){
            System.out.println(node.getPos());
        }
    }

}