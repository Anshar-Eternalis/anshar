package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.mixin.accessor.ServerPlayNetworkHandlerAccessor;

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
    private boolean inNetwork;

    public PlayerTransportComponent(PlayerEntity entity) {
        this.player = entity;
        
        
    }

    public void enterNetwork(FrequencyNetwork network, BeaconBlockEntity through) {
        // ServerPlayerEntity
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        
    }

    private boolean isInNetwork() {
        return this.inNetwork;
    }

    public void tick() {
        if (isInNetwork()) {
            if (this.player instanceof ServerPlayerEntity servPlayer) {
                // prevent flying kick
                ((ServerPlayNetworkHandlerAccessor)(Object)servPlayer.networkHandler).anshar$setFloatingTicks(0);
            }
        }
    }
}
