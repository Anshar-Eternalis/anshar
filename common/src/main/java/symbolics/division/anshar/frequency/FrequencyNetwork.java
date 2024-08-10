package symbolics.division.anshar.frequency;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.Nullable;
import symbolics.division.anshar.beacon.BeaconComponent;
import symbolics.division.anshar.storage.EmbeddedStorage;

import java.util.*;

public class FrequencyNetwork {

    public static final Codec<FrequencyNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(FrequencyNetwork::getId),
        FrequencyIdentifier.CODEC.fieldOf("frequency").forGetter(FrequencyNetwork::getFreqID),
        EmbeddedStorage.CODEC.fieldOf("storage").forGetter(FrequencyNetwork::getStorage),
        FrequencyNode.CODEC.listOf().fieldOf("nodes").forGetter(network -> network.getAllNodes().stream().toList())
    ).apply(instance, FrequencyNetwork::new));

    private final UUID id;
    private final FrequencyIdentifier freqID;
    private EmbeddedStorage storage;
    private final Map<BlockPos, FrequencyNode> nodes = new HashMap<>();
//    private final Map<BlockPos, BeaconNode> beacons = new Object2ReferenceOpenHashMap<>();

    public FrequencyNetwork(UUID id, FrequencyIdentifier FreqID) {
        this.id = id;
        this.freqID = FreqID;
    }

    private FrequencyNetwork(UUID id, FrequencyIdentifier frequency, EmbeddedStorage storage, List<FrequencyNode> nodes) {
        this(id, frequency);
        this.storage = storage;
        nodes.forEach(node -> this.nodes.put(node.getPos(), node));
    }

    public UUID getId() {
        return id;
    }

    public FrequencyIdentifier getFreqID() {
        return freqID;
    }

    public Set<BlockPos> getNodes() {
        return Collections.unmodifiableSet(this.nodes.keySet());
    }

    public boolean removeNode (BlockPos beaconPos) {
        return nodes.remove(beaconPos) != null;
    }

    public boolean removeNode (BeaconComponent beaconComponent) {
        return nodes.remove(beaconComponent.getBeaconPos()) != null;
    }

    protected boolean addNode (BeaconComponent beaconComponent) {
        FrequencyNode node = new FrequencyNode(beaconComponent);
        return nodes.put(node.getPos(), node) == null;
    }

    public void updateNode (BeaconComponent beaconComponent) {
        FrequencyNode node = new FrequencyNode(beaconComponent);
        if (nodes.containsKey(node.getPos())) {
            nodes.put(node.getPos(), node);
        }
    }

    @Nullable
    public FrequencyNode getNode(BlockPos pos) {
        return nodes.get(pos);
    }

    public Set<FrequencyNode> getAllNodes() {
        return ImmutableSet.copyOf(nodes.values());
    }

    public EmbeddedStorage getStorage(){
        if (this.storage == null){
            this.storage = new EmbeddedStorage();
        }
        return this.storage;
    }

}
