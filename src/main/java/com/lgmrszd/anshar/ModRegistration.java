package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconEvents;
import com.lgmrszd.anshar.beacon.EndCrystalEvents;
import com.lgmrszd.anshar.beacon.PlayerTransportComponent;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModRegistration {
    public static void registerAll() {
        registerEvents();
        registerCommands();
        ModApi.register();

        ServerPlayNetworking.registerGlobalReceiver(PlayerTransportComponent.JUMP_PACKET_ID, (a, player, b, c, d) -> PlayerTransportComponent.KEY.get(player).tryJump());

        Registry.register(Registries.SOUND_EVENT, ModResources.EMBED_SPACE_AMBIENT_SOUND, ModResources.EMBED_SPACE_AMBIENT_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, ModResources.TRANSPORT_JUMP_SOUND, ModResources.TRANSPORT_JUMP_SOUND_EVENT);
    }

    private static void registerEvents() {
        BeaconEvents.register();
        EndCrystalEvents.register();
    }

    private static void registerCommands() {
        ModCommands.register();
    }
}
