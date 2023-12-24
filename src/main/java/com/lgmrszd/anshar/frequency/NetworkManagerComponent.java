package com.lgmrszd.anshar.frequency;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import java.util.UUID;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class NetworkManagerComponent implements Component {
    public static final ComponentKey<NetworkManagerComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "network_manager"), NetworkManagerComponent.class
    );

    public FrequencyNetwork getNetwork(IFrequencyIdentifier query){
        throw new UnsupportedOperationException("Unimplemented method 'getNetwork'");
    }

    public FrequencyNetwork getNetwork(UUID uuid){
        throw new UnsupportedOperationException("Unimplemented method 'getNetwork'");
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readFromNbt'");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeToNbt'");
    }
}
