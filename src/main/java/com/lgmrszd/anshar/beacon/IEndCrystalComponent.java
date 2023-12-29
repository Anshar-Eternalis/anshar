package com.lgmrszd.anshar.beacon;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public interface IEndCrystalComponent extends ServerTickingComponent {
    ComponentKey<IEndCrystalComponent> KEY = ComponentRegistry.getOrCreate(
            new Identifier(MOD_ID, "end_crystal"), IEndCrystalComponent.class
    );

    public void setBeacon(BlockPos pos);

    Optional<BlockPos> getConnectedBeacon();
}
