package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconEvents;
import com.lgmrszd.anshar.config.ServerConfig;
import com.lgmrszd.anshar.dispenser.ModDispenserBehaviors;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS;
import net.minecraft.util.Identifier;

public class ModRegistration {
    public static void registerAll() {
        BeaconEvents.register();

        ModCommands.register();

        ModApi.register();

        ModDispenserBehaviors.register();

        ModGroup.register();

        ServerPlayNetworking.registerGlobalReceiver(PlayerTransportComponent.JUMP_PACKET_ID, (a, player, b, c, d) -> PlayerTransportComponent.KEY.get(player).tryJump());

        Registry.register(Registries.SOUND_EVENT, ModResources.EMBED_SPACE_AMBIENT_SOUND, ModResources.EMBED_SPACE_AMBIENT_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, ModResources.TRANSPORT_JUMP_SOUND, ModResources.TRANSPORT_JUMP_SOUND_EVENT);

        registerCommands();
        registerEvents();
    }

    private static void registerEvents() {
//        DebugEvents.register();
        BeaconEvents.register();
        SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            ServerConfig.sendNewValue(
                    player,
                    player.getWorld().getGameRules().getInt(Anshar.END_CRYSTAL_LINKING_DISTANCE)
            );
        });
    }

    private static void registerCommands() {
        ModCommands.register();
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Anshar.MOD_ID, "gate_star"), TransportEffects.GATE_STAR);
    }
}
