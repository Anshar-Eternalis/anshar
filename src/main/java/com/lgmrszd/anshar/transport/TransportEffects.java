package com.lgmrszd.anshar.transport;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.particle.SimpleParticleType;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class TransportEffects {
    public static FireworkExplosionComponent makeTransportFirework(int color) {
        FireworkExplosionComponent component = new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.STAR,
                IntList.of(color),
                IntList.of(color),
                false,
                false
        );
        return component;
    }

    // TODO setup to cancel particles
    public static final SimpleParticleType GATE_STAR = FabricParticleTypes.simple(false);
}
