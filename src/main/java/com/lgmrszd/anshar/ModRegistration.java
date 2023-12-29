package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconEvents;
import com.lgmrszd.anshar.beacon.EndCrystalEvents;

public class ModRegistration {
    public static void registerAll() {
        registerEvents();
        registerCommands();
        ModApi.register();
    }

    private static void registerEvents() {
        BeaconEvents.register();
        EndCrystalEvents.register();
    }

    private static void registerCommands() {
        ModCommands.register();
    }
}
