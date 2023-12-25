package com.lgmrszd.anshar.frequency;

import static com.lgmrszd.anshar.Anshar.LOGGER;
import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.*;
import java.util.stream.Collectors;

import com.lgmrszd.anshar.beacon.IBeaconComponent;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

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
            FrequencyNetwork frequencyNetwork = new FrequencyNetwork(randuuid, query);
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

    public Optional<BeaconBlockEntity> getNearestConnectedBeacon(World world, BlockPos pos) {
        if (world == null) return Optional.empty();
        return networksByUUID.values().stream()
                .map(FrequencyNetwork::getBeacons)
                .flatMap(Collection::stream)
                .sorted((pos1, pos2) -> {
                    double distance1 = pos.getSquaredDistance(pos1);
                    double distance2 = pos.getSquaredDistance(pos2);
                    return Double.compare(distance1, distance2);
                })
                .filter(blockPos -> world.isChunkLoaded(
                        ChunkSectionPos.getSectionCoord(blockPos.getX()),
                                ChunkSectionPos.getSectionCoord(blockPos.getY()))
                )
                .map(pos1 -> {
                    return world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe ? bbe : null;
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        // TODO: remove this! It's here for debug for now
        if (true) return;
        try {
            NbtCompound networksTag = tag.getCompound("networks");
            for (String uuid_string : networksTag.getKeys()) {
                UUID uuid = UUID.fromString(uuid_string);
                NbtCompound networkTag = networksTag.getCompound(uuid_string);
                FrequencyNetwork network = FrequencyNetwork.fromNbt(uuid, networkTag);
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
