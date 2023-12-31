package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconEvents;
import com.lgmrszd.anshar.beacon.EndCrystalEvents;
import com.lgmrszd.anshar.beacon.PlayerTransportComponent;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModRegistration {
    public static void registerAll() {
        registerEvents();
        registerCommands();
        ModApi.register();

        ServerPlayNetworking.registerGlobalReceiver(PlayerTransportComponent.JUMP_PACKET_ID, (a, player, b, c, d) -> PlayerTransportComponent.KEY.get(player).tryJump());
    }

    private static void registerEvents() {
        BeaconEvents.register();
        EndCrystalEvents.register();
    }

    private static void registerCommands() {
        ModCommands.register();
    }
}
