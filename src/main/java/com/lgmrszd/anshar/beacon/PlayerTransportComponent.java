package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/*
 * Manages player transport between beacons via Star Gates.
 * 
 * Manages both logic and state.
 */

public class PlayerTransportComponent implements Component {

    public static final ComponentKey<PlayerTransportComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "player_transport"), PlayerTransportComponent.class
    );

    boolean state = false;

    public void addPlayerToNetwork(FrequencyNetwork network) {
        if (!state){
            System.out.println("adding to network");
            state = true;
        }
    }

    public void removePlayerFromNetwork() {
        if (state){
            System.out.println("removing from freq");
            state = false;
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        
    }


}
