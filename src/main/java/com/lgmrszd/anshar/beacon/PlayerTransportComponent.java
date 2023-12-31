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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        KEY.sync(player);
        sendExplosionPacketS2C();
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
        if (tag != null) {
            this.readFromNbt(tag);
        }
    }

    public final boolean isInNetwork() {
        return networkUUID != null;
    }

    private void moveToCurrentTarget() {
        this.player.teleport(target.getX(), 10000, target.getZ());
    }

    private static final double MIN_NODE_SEPARATION_RADS = Math.PI * 0.25;
    private static final double MIN_NODE_SEPARATION_DIST = 5;
    private void updateJumpNodes() {
        // get nearest nodes in each direction, pruning those too close together or close to the player
        if (isClient) return;

        var networkOpt = getNetwork();
        if (networkOpt.isPresent()) {
            var network = networkOpt.get();
            var out = new HashSet<BeaconNode>();
            // get list of nodes outside minimum range, sorted by distance from player
            var candidates = network.getAllNodes().stream().sorted((a, b) -> Double.compare(distanceTo(a), distanceTo(b))).filter(
                node -> distanceTo(node) > MIN_NODE_SEPARATION_DIST
            );
            // starting from nearest, add to output set as long as not too close to any others in the set
            for (BeaconNode node : candidates.collect(Collectors.toList())) {
                boolean valid = true;
                for (BeaconNode prev : out) {
                    var sep = Math.acos(normedVecToNode(prev).dotProduct(normedVecToNode(node)));
                    if (sep < MIN_NODE_SEPARATION_RADS) {
                        valid = false;
                        break;
                    }
                }
                if (valid) out.add(node);
            }
            jumpCandidates = out;
        } else {
            jumpCandidates.clear();
        }
        
        KEY.sync(player);
    }

    public Set<BeaconNode> getJumpCandidates() {
        return jumpCandidates;
    }

    @Override
    public void serverTick() {        
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
                updateJumpNodes();
                // var ppos = player.getPos();
                // for (var node : jumpCandidates) {
                //     // get vector from player to node pos
                //     var spos = normedVecToNode(node).multiply(2).add(ppos);
                //     serverPlayer.getServerWorld().spawnParticles(serverPlayer, ParticleTypes.HEART, true, spos.getX(), serverPlayer.getEyeY(), spos.getZ(), 5, 0, 0, 0, 0);
                // }
            }
        }
    }

    public Vec3d normedVecToNode(BeaconNode node) {
        // produces a normal vector from the player to the node that is flattened on the xz-plane
        var nVec = node.getPos().toCenterPos();
        var vec = new Vec3d(nVec.getX(), player.getPos().getY(), nVec.getZ()).subtract(player.getPos());
        return vec.multiply(1/vec.length());
    }

    public double distanceTo(BeaconNode node) {
        var n = node.getPos().toCenterPos().subtract(player.getPos());
        return Math.sqrt(Math.pow(n.getX(), 2) + Math.pow(n.getZ(), 2));
    }

    public final BeaconNode getNearestLookedAt() {
        // find nearest node looked at
        Vec3d lookVec = player.getRotationVector();
        double distance = 0;
        BeaconNode nearest = null;
        for (BeaconNode node : jumpCandidates) {
            
            var proj = normedVecToNode(node).dotProduct(lookVec);
            if (proj > distance) {
                distance = proj;
                nearest = node;
            }
        }
        return nearest;
    };

    public final boolean tryJump() {
        var nearest = getNearestLookedAt();
        if (nearest != null) {
            target = nearest.getPos();
            jumpCandidates.clear();
            KEY.sync(player);
            return true;
        }
        return false;
        
    }

    public static final Identifier JUMP_PACKET_ID = new Identifier(MOD_ID, "player_transport_jump");
    public static final Identifier EXPLOSION_PACKET_ID = new Identifier(MOD_ID, "player_transport_explosion");

    public void sendExplosionPacketS2C(){
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(target);
        for (var player : player.getWorld().getPlayers()) {
            ServerPlayNetworking.send((ServerPlayerEntity)player, EXPLOSION_PACKET_ID, buf);
        }
    }
}
