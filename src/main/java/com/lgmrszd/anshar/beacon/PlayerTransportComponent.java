package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.lang.ref.WeakReference;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.mixin.accessor.ServerPlayNetworkHandlerAccessor;
import com.lgmrszd.anshar.util.WeakRef;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.fabricmc.fabric.mixin.event.interaction.ServerPlayNetworkHandlerMixin;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayNetworkHandler;
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

public class PlayerTransportComponent implements Component {

    public static final ComponentKey<PlayerTransportComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "player_transport"), PlayerTransportComponent.class
    );

    private final PlayerEntity player;
    private WeakRef<FrequencyNetwork> network = new WeakRef<>(null);
    private BlockPos target; // make optional? idk

    public PlayerTransportComponent(PlayerEntity entity) {
        this.player = entity;
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

    public void enterNetwork(FrequencyNetwork network, BeaconBlockEntity through) {
        // network enter animation
        // transition to embed state
        this.network = new WeakRef<FrequencyNetwork>(network);
        this.target = through.getPos();
        moveToCurrentTarget();
    }

    private void exitNetwork() {
        moveToCurrentTarget();
        this.player.teleport(target.getX() + 1, target.getY(), target.getZ());
        this.network.clear();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        
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
                // prevent flying kick
                ((ServerPlayNetworkHandlerAccessor)(Object)servPlayer.networkHandler).anshar$setFloatingTicks(0);
            }

            if (player.isSneaking()) {
                exitNetwork();
            } else {
                moveToCurrentTarget();
            }
        }
    }
}
