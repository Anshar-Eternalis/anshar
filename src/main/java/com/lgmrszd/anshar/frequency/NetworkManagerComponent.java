package com.lgmrszd.anshar.frequency;

import static com.lgmrszd.anshar.Anshar.LOGGER;
import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;

public class NetworkManagerComponent implements Component {
    private final HashMap<UUID, FrequencyNetwork> networksByUUID;
    public static final ComponentKey<NetworkManagerComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "network_manager"), NetworkManagerComponent.class
    );

    public NetworkManagerComponent() {
        networksByUUID = new HashMap<>();
    }

    public FrequencyNetwork getOrCreateNetwork(IFrequencyIdentifier query){
        List<FrequencyNetwork> matchedNetworks = networksByUUID.values().stream()
                .filter(frequencyNetwork -> frequencyNetwork.getFreqID().equals(query))
                .toList();
        if (matchedNetworks.isEmpty()) {
            UUID randuuid = UUID.randomUUID();
            while (networksByUUID.containsKey(randuuid)) randuuid = UUID.randomUUID();
            FrequencyNetwork frequencyNetwork = new FrequencyNetwork(randuuid);
            networksByUUID.put(randuuid, frequencyNetwork);
            return frequencyNetwork;
        }
        if (matchedNetworks.size() > 1) {
            LOGGER.error("More than one networks detected with given frequency identifier!");
            matchedNetworks.forEach(frequencyNetwork ->
                    LOGGER.error(String.format(
                            "UUID: %s, Number of beacons: %d",
                            frequencyNetwork.getId(),
                            frequencyNetwork.getBeacons().size()
                    )));
        }
        return matchedNetworks.get(0);
    }

    public Optional<FrequencyNetwork> getNetwork(UUID uuid){
        return Optional.ofNullable(networksByUUID.get(uuid));
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        try {
            NbtCompound networksTag = tag.getCompound("networks");
            for (String uuid_string : networksTag.getKeys()) {
                UUID uuid = UUID.fromString(uuid_string);
                FrequencyNetwork network = new FrequencyNetwork(uuid);
                NbtCompound networkTag = networksTag.getCompound(uuid_string);
                network.readFromNbt(networkTag);
//            if (network.getId() != uuid) {
//                LOGGER.warn("Error adding network: stored key ");
//            }
                networksByUUID.put(uuid, network);
            }
        } catch (CrashException e) {
            LOGGER.error("Error while reading Network data from world save!");
            LOGGER.error("crash report as follows:");
            LOGGER.error("\n"+e.getReport().asString());
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtCompound networksTag = new NbtCompound();
        networksByUUID.forEach((uuid, network) -> {
            NbtCompound networkTag = new NbtCompound();
            network.writeToNbt(networkTag);
            networksTag.put(uuid.toString(), networkTag);
        });
        tag.put("networks", networksTag);
    }
}
