package com.lgmrszd.anshar;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import net.minecraft.block.entity.BeaconBlockEntity;

import com.lgmrszd.anshar.frequency.HashFrequencyIdentifierComponent;
import com.lgmrszd.anshar.frequency.IFrequencyIdentifierComponent;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.IBeaconComponent;

public final class ModComponents implements BlockComponentInitializer {

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(BeaconBlockEntity.class, IFrequencyIdentifierComponent.KEY, b -> new HashFrequencyIdentifierComponent());
        registry.registerFor(BeaconBlockEntity.class, IBeaconComponent.KEY, BeaconComponent::new);
    }
}
