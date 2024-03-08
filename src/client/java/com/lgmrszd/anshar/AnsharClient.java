package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconComponentClient;
import com.lgmrszd.anshar.beacon.EndCrystalComponentClient;
import com.lgmrszd.anshar.debug.DebugClient;
import com.lgmrszd.anshar.debug.DebugRenderer;
import com.lgmrszd.anshar.transport.PlayerTransportClient;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;
import com.lgmrszd.anshar.transport.TransportGateParticle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.network.ClientPlayerEntity;

public class AnsharClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(
			PlayerTransportComponent.EXPLOSION_PACKET_ID, 
			PlayerTransportClient::acceptExplosionPacketS2C
		);

		BeaconComponentClient.init();
		EndCrystalComponentClient.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			BeaconComponentClient.clientGlobalTick();
		});

		ParticleFactoryRegistry.getInstance().register(TransportEffects.GATE_STAR, TransportGateParticle.Factory::new);

		ClientEntityEvents.ENTITY_LOAD.register((entity, clientWorld) -> {
			if (entity instanceof ClientPlayerEntity player) {
				var transport = PlayerTransportComponent.KEY.get(player);
				transport.setClientEnterCallback(PlayerTransportClient::enterNetworkCallback);
				transport.setClientTickCallback(PlayerTransportClient::tickCallback);
				transport.setClientExitCallback(PlayerTransportClient::exitNetworkCallback);
			}
		});

		DebugRenderer.init();
		DebugClient.init();

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
//			Matrix4f positionMatrix = drawContext.getMatrices().peek().getPositionMatrix();
//			Tessellator tessellator = Tessellator.getInstance();
//			BufferBuilder buffer = tessellator.getBuffer();
//
//			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
//			buffer.vertex(positionMatrix, 20, 20, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).next();
//			buffer.vertex(positionMatrix, 20, 60, 0).color(1f, 0f, 0f, 1f).texture(0f, 1f).next();
//			buffer.vertex(positionMatrix, 60, 60, 0).color(0f, 1f, 0f, 1f).texture(1f, 1f).next();
//			buffer.vertex(positionMatrix, 60, 20, 0).color(0f, 0f, 1f, 1f).texture(1f, 0f).next();
//
//			RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
//			RenderSystem.setShaderTexture(0, new Identifier(MOD_ID, "icon.png"));
//			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//
//			tessellator.draw();
		});
	}
}