package com.lgmrszd.anshar.transport;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.particle.SimpleParticleType;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class TransportEffects {
    public static FireworkExplosionComponent makeTransportFirework(int color) {
        return new FireworkExplosionComponent(
                FireworkExplosionComponent.Type.LARGE_BALL,
                IntList.of(color),
                IntList.of(color),
                false,
                false
        );
    }

    // TODO setup to cancel particles
    public static final SimpleParticleType GATE_STAR = FabricParticleTypes.simple(false);
}
