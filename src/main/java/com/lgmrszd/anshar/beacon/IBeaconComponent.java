package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.frequency.IFrequencyIdentifier;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

public interface IBeaconComponent extends ClientTickingComponent, ServerTickingComponent, AutoSyncedComponent {
    ComponentKey<IBeaconComponent> KEY = ComponentRegistry.getOrCreate(
        Identifier.of(MOD_ID, "beacon"), IBeaconComponent.class
    );

//    void rescanPyramid();

    boolean isValid();
    boolean isActive();
    IFrequencyIdentifier getEffectiveFrequencyID();

    Optional<FrequencyNetwork> getFrequencyNetwork();

    BlockPos getBeaconPos();

    Text getName();

    void tryPutPlayerIntoNetwork(ServerPlayerEntity player);

    float[] topColor();

    List<IEndCrystalComponent> getConnectedEndCrystals();
}