package com.lgmrszd.anshar;

import com.lgmrszd.anshar.freq.IBeaconComponent;
import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.util.Identifier;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

public class MyComponents implements BlockComponentInitializer {

    public static final ComponentKey<IBeaconComponent> BEACON =
            ComponentRegistry.getOrCreate(new Identifier(MOD_ID, "beacon"), IBeaconComponent.class);

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(BeaconBlockEntity.class, BEACON, BeaconComponent::new);
    }
}
