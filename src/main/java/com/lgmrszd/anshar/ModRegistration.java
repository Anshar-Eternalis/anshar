package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconEvents;

public class ModRegistration {
    public static void registerAll() {
        registerEvents();
        registerCommands();
    }

    private static void registerEvents() {
        BeaconEvents.register();
    }

    private static void registerCommands() {
        ModCommands.register();
    }
}
