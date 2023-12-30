package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.LOGGER;
import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.Optional;
import java.util.Set;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.mixin.accessor.ServerPlayNetworkHandlerAccessor;
import com.lgmrszd.anshar.util.WeakRef;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
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

public class PlayerTransportComponent implements AutoSyncedComponent {

    public static final ComponentKey<PlayerTransportComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "player_transport"), PlayerTransportComponent.class
    );

    private final PlayerEntity player;
    private WeakRef<FrequencyNetwork> network = new WeakRef<>(null);
    private BlockPos target; // make optional? idk
    private final boolean isClient;

    public PlayerTransportComponent(PlayerEntity player) {
        this.player = player;
        this.isClient = player.getServer() == null;
    }

    private NetworkManagerComponent getNetworkManager(){
        return NetworkManagerComponent.KEY.get(this.player.getWorld().getLevelProperties());
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
        // network enter animation
        // transition to embed state
        this.network = new WeakRef<FrequencyNetwork>(network);
        this.target = through;
        if (!isClient) {
            getJumpNodes().ifPresent(nodes -> PlayerTransportNetworking.sendNodeListS2C((ServerPlayerEntity)player, nodes));
        }
    }

    private void exitNetwork() {
        moveToCurrentTarget();
        this.player.teleport(target.getX() + 1, target.getY(), target.getZ());
        this.network.clear();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (!isClient) getNetworkManager().getNetwork(tag.getUuid("network")).ifPresent(net -> {
            var tgt = tag.getIntArray("target");
            if (tgt != null && tgt.length == 3) {
                enterNetwork(net, new BlockPos(tgt[0], tgt[1], tgt[2]));
            } else {
                LOGGER.debug("Player transport component tried to enter network of id [" + net.getId() + "] with invalid target coords: " + tgt);
            }
        });
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (!isClient && isInNetwork()) this.network.ifPresent(net -> {
            tag.putUuid("network", net.getId());
            tag.putIntArray("target", new int[] {target.getX(), target.getY(), target.getZ()});
        });
    }

    private boolean isInNetwork() {
        return !this.network.refersTo(null);
    }

    private void moveToCurrentTarget() {
        this.player.teleport(target.getX(), 1000, target.getZ());
    }

    public void tick() {
        if (isInNetwork()) { // maintain embed state
            // disable appearance and gravity
            if (this.player instanceof ServerPlayerEntity servPlayer) {
                // prevent flying kick (not the bruce lee kind)
                ((ServerPlayNetworkHandlerAccessor)(Object)servPlayer.networkHandler).anshar$setFloatingTicks(0);
            }

            if (player.isSneaking()) {
                exitNetwork();
            } else {
                moveToCurrentTarget();
            }
        }
    }

    private static final double MIN_NODE_SEPARATION_RADS = 0.1;
    private Optional<Set<BeaconNode>> getJumpNodes() {
        // get nearest nodes in each direction, pruning those too close together
        // TODO: do the above, right now I just get all lol
        return Optional.of(this.network.map(net -> net.getAllNodes()));
    }

    
}
