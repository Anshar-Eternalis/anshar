package com.lgmrszd.anshar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AnsharClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_WORLD_TICK.register(PlayerTransportClient::tick);
	}

}