package com.lgmrszd.anshar.util;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class RegistryUtil {
    private static World cachedWorld;

    public static void setCachedWorld(World world) {
        cachedWorld = world;
    }

    public static RegistryWrapper.WrapperLookup getRegistries() {
        return cachedWorld.getRegistryManager();
    }
}
