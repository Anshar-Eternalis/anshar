package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.mixin.accessor.ServerPlayNetworkHandlerAccessor;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/*
 * Manages player transport between beacons via Star Gates.
 * 
 * Manages both logic and state.
 * 
 * flow:
 *  - player steps on beacon, is added to network and enters astral realm
 *  - in astral realm, player travels through stargates to other beacons
 *  - player presses shift to descend once at desired beacon
 * 
 */

public class PlayerTransportComponent implements ServerTickingComponent, AutoSyncedComponent {

    public static final ComponentKey<PlayerTransportComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "player_transport"), PlayerTransportComponent.class
    );

    private final PlayerEntity player;
    private UUID networkUUID;
    private BlockPos target; // make optional? idk
    private final boolean isClient;
    private Set<BeaconNode> jumpCandidates = new HashSet<>();

    public PlayerTransportComponent(PlayerEntity player) {
        this.player = player;
        this.target = player.getBlockPos();
        this.isClient = player.getServer() == null;
    }

    private NetworkManagerComponent getNetworkManager(){
        return NetworkManagerComponent.KEY.get(this.player.getWorld().getLevelProperties());
    }

    private Optional<FrequencyNetwork> getNetwork(){
        return getNetworkManager().getNetwork(networkUUID);
    }

    /*
    - teleport to sky
    - disable gravity
    - show targets
    - states: 
    start -> fly up -> select -> move -> (loop) select
                            |-> fly down -> end
    + disable gravity during embed state (isInNetwork)
    + play animations during states
    */

    public void enterNetwork(FrequencyNetwork network, BlockPos through) {
        // called on server when player steps on beacon
        this.networkUUID = network.getId();
        this.target = through;

        // call on next server tick to make sure things are loaded
        // enterNetworkCallback = () -> {
        //     this.target = through;
        //     getJumpNodes().ifPresent(nodes -> PlayerTransportNetworking.sendNodeListS2C((ServerPlayerEntity)player, nodes));
        // };
        
        KEY.sync(player);
        System.out.println((isClient ? "(client)" : "(server)") + "entered network " + networkUUID);
    }

    private void exitNetwork() {
        moveToCurrentTarget();
        this.player.teleport(target.getX() + 1, target.getY(), target.getZ());
        this.networkUUID = null;
        this.target = null;
        if (player.isInvisible()) player.setInvisible(false);
        KEY.sync(player);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.containsUuid("network")) {
            networkUUID = tag.getUuid("network");
            if (tag.contains("target")) this.target = BlockPos.fromLong(tag.getLong("target"));
            if (tag.contains("jump_targets")) {
                this.jumpCandidates = new HashSet<>();
                tag.getList("jump_targets", NbtElement.COMPOUND_TYPE).forEach(
                    element -> jumpCandidates.add(BeaconNode.fromNBT((NbtCompound)element))
                );
            }
        } else {
            networkUUID = null;
            target = null;
            jumpCandidates = new HashSet<>();
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (networkUUID != null) {
            tag.putUuid("network", networkUUID);
            if (target != null) tag.putLong("target", target.asLong());
            
            var nodeList = new NbtList();
            for (BeaconNode node : jumpCandidates) nodeList.add(node.toNBT());
            tag.put("jump_targets", nodeList);
        }
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity serverPlayer) { return player == serverPlayer; }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        NbtCompound tag = buf.readNbt();
        var oldtgt = target;
        if (tag != null) {
            this.readFromNbt(tag);
        }

        if (isClient) {
            // System.out.println("(client) participating in network " + networkUUID + " with target " + target);
            // if (jumpCandidates.size() > 0) {
            //     System.out.println("(client) jump targets: ");
            //     for (var node : jumpCandidates) {
            //         System.out.println("Name: [" + node.getName().getString() + "], Pos: [" + node.getPos() + "]");
            //     }
            // }
        } else {
            System.out.println("(server) got sync packet from client");
            System.out.println("(server) old target: " + oldtgt);
            System.out.println("(server) new target: " + target);
        }
    }

    public final boolean isInNetwork() {
        return networkUUID != null;
    }

    private void moveToCurrentTarget() {
        this.player.teleport(target.getX(), 200, target.getZ());
    }

    private static final double MIN_NODE_SEPARATION_RADS = 0.1;
    private static final double MIN_NODE_SEPARATION_DIST = 5;
    private void updateJumpNodes() {
        // get nearest nodes in each direction, pruning those too close together or close to the player
        if (isClient) return;
        jumpCandidates = getNetwork().map(network -> {
            var out = new HashSet<BeaconNode>();
            // get list of nodes outside minimum range, sorted by distance from player
            var candidates = network.getAllNodes().stream().sorted((a, b) -> Integer.compare(distanceTo(a), distanceTo(b))).filter(
                node -> distanceTo(node) > MIN_NODE_SEPARATION_DIST
            );
            // starting from nearest, add to output set as long as not too close to any others in the set
            for (BeaconNode node : candidates.collect(Collectors.toList())) {
                boolean valid = true;
                for (BeaconNode prev : out) {
                    if (Math.abs(normedVecToNode(prev).dotProduct(normedVecToNode(node))) < MIN_NODE_SEPARATION_RADS) {
                        valid = false;
                        break;
                    }
                }
                if (valid) out.add(node);
            }
            return out;
        }).orElse(new HashSet<>());
        KEY.sync(player);
    }

    private Runnable enterNetworkCallback;
    @Override
    public void serverTick() {
        if (enterNetworkCallback != null) {
            enterNetworkCallback.run();
            enterNetworkCallback = null;
        }
        
        if (isInNetwork()) { // maintain embed state
            // disable appearance and gravity
            var serverPlayer = (ServerPlayerEntity)player;

            // prevent flying kick
            ((ServerPlayNetworkHandlerAccessor)serverPlayer.networkHandler).anshar$setFloatingTicks(0);

            if (player.isSneaking()) {
                exitNetwork();
            } else {
                if (!player.isInvisible()) player.setInvisible(true);
                moveToCurrentTarget();
            }
            
            // draw nearby nodes
            if (serverPlayer.getWorld().getTime() % 10 == 0) {
                var ppos = player.getPos();
                updateJumpNodes();
                for (var node : jumpCandidates) {
                    // get vector from player to node pos
                    var spos = normedVecToNode(node).multiply(2).add(ppos);
                    serverPlayer.getServerWorld().spawnParticles(serverPlayer, ParticleTypes.HEART, true, spos.getX(), spos.getY(), spos.getZ(), 5, 0, 0, 0, 0);
                }
            }
        }
    }

    private Vec3d normedVecToNode(BeaconNode node) {
        // produces a normal vector from the player to the node that is flattened on the xz-plane
        var nVec = node.getPos().toCenterPos();
        var vec = new Vec3d(nVec.getX(), player.getPos().getY(), nVec.getZ()).subtract(player.getPos());
        return vec.multiply(1/vec.length());
    }

    private int distanceTo(BeaconNode node) {
        return player.getBlockPos().getManhattanDistance(node.getPos());
    }

    public final boolean tryJump() {
        // find nearest node looked at
        Vec3d lookVec = player.getRotationVector();
        System.out.println("attempting jump");
        double distance = 0;
        BeaconNode nearest = null;
        for (BeaconNode node : jumpCandidates) {
            var proj = normedVecToNode(node).dotProduct(lookVec);
            if (proj > distance) {
                distance = proj;
                nearest = node;
            }
        }

        
        if (nearest != null) {
            System.out.println("nearest beacon is at " + nearest.getPos());
            target = nearest.getPos();
            KEY.sync(player);
            return true;
        }
        System.out.println("failed to find a nearest beacon");
        return false;
        
    }

    public static final Identifier JUMP_PACKET_ID = new Identifier(MOD_ID, "player_transport_jump");

    
}
