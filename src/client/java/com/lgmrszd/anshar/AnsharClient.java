package com.lgmrszd.anshar;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
public class AnsharClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_WORLD_TICK.register(PlayerTransportClient::tick);

		ClientPlayNetworking.registerGlobalReceiver(
			PlayerTransportComponent.EXPLOSION_PACKET_ID, 
			PlayerTransportClient::acceptExplosionPacketS2C
		);
	}

}