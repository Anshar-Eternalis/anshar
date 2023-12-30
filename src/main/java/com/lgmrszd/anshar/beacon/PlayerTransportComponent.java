package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.mixin.accessor.ServerPlayNetworkHandlerAccessor;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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

public class PlayerTransportComponent implements ServerTickingComponent, ClientTickingComponent, AutoSyncedComponent {

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
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (networkUUID != null) {
            tag.putUuid("network", networkUUID);
            if (target != null) tag.putLong("target", target.asLong());
            
            getJumpNodes().ifPresent(nodes -> {
                var nodeList = new NbtList();
                for (BeaconNode node : nodes) nodeList.add(node.toNBT());
                tag.put("jump_targets", nodeList);
            });
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

        if (isClient) {
            System.out.println("(client) participating in network " + networkUUID + " with target " + target);
            if (jumpCandidates.size() > 0) {
                System.out.println("(client) jump targets: ");
                for (var node : jumpCandidates) {
                    System.out.println("Name: [" + node.getName().getString() + "], Pos: [" + node.getPos() + "]");
                }
            }
        }
    }

    private boolean isInNetwork() {
        return networkUUID != null;
    }

    private void moveToCurrentTarget() {
        this.player.teleport(target.getX(), 1000, target.getZ());
    }

    private static final double MIN_NODE_SEPARATION_RADS = 0.1;
    private Optional<Set<BeaconNode>> getJumpNodes() {
        // get nearest nodes in each direction, pruning those too close together
        // TODO: do the above, right now I just get all lol
        return getNetwork().map(net -> net.getAllNodes());
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
            
        }
    }

    @Override
    public void clientTick() {
        
    }
}
