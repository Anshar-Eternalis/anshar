package com.lgmrszd.anshar.EndCrystal;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public interface IEndCrystalComponent extends Component {
    ComponentKey<IEndCrystalComponent> KEY = ComponentRegistry.getOrCreate(
            new Identifier(MOD_ID, "end_crystal"), IEndCrystalComponent.class
    );

    Optional<BlockPos> getConnectedBeacon();
}
