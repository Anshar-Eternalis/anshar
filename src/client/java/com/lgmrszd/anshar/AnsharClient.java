package com.lgmrszd.anshar;

import com.lgmrszd.anshar.config.client.ServerConfigSync;
import com.lgmrszd.anshar.transport.PlayerTransportClient;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;
import com.lgmrszd.anshar.transport.TransportGateParticle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.network.ClientPlayerEntity;

public class AnsharClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(
			PlayerTransportComponent.EXPLOSION_PACKET_ID, 
			PlayerTransportClient::acceptExplosionPacketS2C
		);

		ServerConfigSync.registerReceivers();

		ParticleFactoryRegistry.getInstance().register(TransportEffects.GATE_STAR, TransportGateParticle.Factory::new);

		ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
			if (entity instanceof ClientPlayerEntity player) {
				var transport = PlayerTransportComponent.KEY.get(player);
				transport.setClientEnterCallback(PlayerTransportClient::enterNetworkCallback);
				transport.setClientTickCallback(PlayerTransportClient::tickCallback);
			}
		});
	}

}