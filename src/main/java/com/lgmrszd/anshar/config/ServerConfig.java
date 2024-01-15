package com.lgmrszd.anshar.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static ModConfigSpec.IntValue endCrystalMaxDistance;
    public static ModConfigSpec.IntValue endCrystalsPerBeacon;

    static {
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        setupConfig(configBuilder);
        CONFIG_SPEC = configBuilder.build();
    }

    private static void setupConfig(ModConfigSpec.Builder builder) {
        builder.comment("Category for End Crystal related things");
        builder.push("End Crystal options");
        endCrystalMaxDistance = builder
                .comment("Max distance at which End Crystals are allowed to connect to the Beacons")
                .comment("Existing crystals won't disconnect if the value is decreased!")
                .defineInRange("end_crystal_max_distance", 16, 4, 64);
        endCrystalsPerBeacon = builder
                .comment("How many End Crystals can be connected to the same Beacon at once?")
                .comment("Existing crystals won't disconnect if the value is decreased!")
                .defineInRange("end_crystals_per_beacon", 4, 0, 64);
        builder.pop();
    }
}
