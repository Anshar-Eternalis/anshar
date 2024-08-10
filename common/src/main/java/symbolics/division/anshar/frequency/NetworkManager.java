package symbolics.division.anshar.frequency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import symbolics.division.anshar.beacon.BeaconComponent;

import java.util.*;
import java.util.function.Consumer;

import static symbolics.division.anshar.Anshar.LOGGER;

public class NetworkManager {

    public static final Codec<NetworkManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FrequencyNetwork.CODEC.listOf().fieldOf("networks").forGetter(manager -> manager.getNetworks().stream().toList())
    ).apply(instance, NetworkManager::new));

    private final HashMap<UUID, FrequencyNetwork> networksByUUID;

    public NetworkManager() {
        networksByUUID = new HashMap<>();
    }

    private NetworkManager(List<FrequencyNetwork> networks) {
        this();
        networks.forEach(network -> networksByUUID.put(network.getId(), network));
    }

    public Collection<FrequencyNetwork> getNetworks() {
        return networksByUUID.values();
    }

    private FrequencyNetwork getOrCreateNetwork(FrequencyIdentifier query){
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
            LOGGER.error("More than one network detected with given frequency identifier!");
            matchedNetworks.forEach(frequencyNetwork ->
                    LOGGER.error(String.format(
                            "UUID: %s, Number of nodes: %d",
                            frequencyNetwork.getId(),
                            frequencyNetwork.getNodes().size()
                    )));
        }
        return matchedNetworks.get(0);
    }

    public Optional<FrequencyNetwork> getNetwork(UUID uuid){
        return Optional.ofNullable(networksByUUID.get(uuid));
    }

    // todo: getNearestConnectedNode?
    public Optional<BeaconBlockEntity> getNearestConnectedBeacon(Level world, BlockPos pos) {
        if (world == null) return Optional.empty();
        ResourceKey<Level> dim = world.dimension();
        return networksByUUID.values().stream()
                .filter(frequencyNetwork -> frequencyNetwork.getFreqID().isValidInDimension(dim))
                .map(FrequencyNetwork::getNodes)
                .flatMap(Collection::stream)
                .filter(blockPos -> world.hasChunk(
                        SectionPos.blockToSectionCoord(blockPos.getX()),
                        SectionPos.blockToSectionCoord(blockPos.getZ()))
                )
                .sorted((pos1, pos2) -> {
                    double distance1 = pos.distSqr(pos1);
                    double distance2 = pos.distSqr(pos2);
                    return Double.compare(distance1, distance2);
                })
                .map(pos1 -> world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe ? bbe : null)
                .filter(Objects::nonNull)
                .findFirst();
    }

    public List<BeaconBlockEntity> getConnectedBeaconsInRadius(Level world, BlockPos pos, Double radius) {
        if (world == null) return Collections.emptyList();
        ResourceKey<Level> dim = world.dimension();
        return networksByUUID.values().stream()
                .filter(frequencyNetwork -> frequencyNetwork.getFreqID().isValidInDimension(dim))
                .map(FrequencyNetwork::getNodes)
                .flatMap(Collection::stream)
                .filter(blockPos -> pos.closerThan(blockPos, radius) && world.hasChunk(
                        SectionPos.blockToSectionCoord(blockPos.getX()),
                        SectionPos.blockToSectionCoord(blockPos.getZ()))
                )
                .map(pos1 -> world.getBlockEntity(pos1) instanceof BeaconBlockEntity bbe ? bbe : null)
                .filter(Objects::nonNull)
                .toList();
    }

    public void updateNetwork(BeaconComponent beaconComponent, FrequencyIdentifier frequencyIdentifier, Consumer<FrequencyNetwork> consumer) {
        // Check if it's actually the same frequency as before
        if (beaconComponent.getFrequencyNetwork()
                // continue if there's network with different frequency
                .map(frequencyNetwork -> frequencyNetwork.getFreqID().equals(frequencyIdentifier))
                // continue if no network on the beacon component
                .orElse(false)
        ) return;
        BlockPos beaconPos = beaconComponent.getBeaconPos();
        // Remove beacon from the old network if present
        // TODO: Just to make it safe for now
//        beaconComponent.getFrequencyNetwork().ifPresent(frequencyNetwork -> {
//            if (!frequencyNetwork.removeBeacon(beaconPos)) {
//                LOGGER.warn(String.format(
//                        "ANSHAR WARNING: Tried to remove beacon %s from network %s but it's already not there",
//                        beaconPos,
//                        frequencyNetwork
//                ));
//            }
//        });
        getNetworks().forEach(frequencyNetwork -> {
            frequencyNetwork.removeNode(beaconPos);
        });
        // Add beacon to the new network
        if (frequencyIdentifier.isValid()) {
            FrequencyNetwork network = getOrCreateNetwork(frequencyIdentifier);
            if (!network.addNode(beaconComponent)) {
                LOGGER.warn(String.format(
                        "Tried to add beacon %s to network %s but it's already there!",
                        beaconPos,
                        network
                ));
            }
            consumer.accept(network);
        } else consumer.accept(null);
    }
}
