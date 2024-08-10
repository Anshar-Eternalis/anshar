package symbolics.division.anshar.platform.services;

import net.minecraft.server.MinecraftServer;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

//    // for de/serializing global data
//    void onServerStarted(MinecraftServer server);
}