package symbolics.division.anshar.platform;

import symbolics.division.anshar.Anshar;
import symbolics.division.anshar.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Anshar.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}