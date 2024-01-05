package com.lgmrszd.anshar;

import com.lgmrszd.anshar.config.client.ServerConfigSync;
import com.lgmrszd.anshar.transport.PlayerTransportClient;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;
import com.lgmrszd.anshar.transport.TransportGateParticle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class AnsharClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_WORLD_TICK.register(PlayerTransportClient::tick);

		ClientPlayNetworking.registerGlobalReceiver(
			PlayerTransportComponent.EXPLOSION_PACKET_ID, 
			PlayerTransportClient::acceptExplosionPacketS2C
		);

		ServerConfigSync.registerReceivers();

		ParticleFactoryRegistry.getInstance().register(TransportEffects.GATE_STAR, TransportGateParticle.Factory::new);
	}

}