package com.lgmrszd.anshar.transport;

import it.unimi.dsi.fastutil.ints.IntList;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.particle.SimpleParticleType;

import java.util.Collections;
import java.util.List;

public class TransportEffects {
    public static List<FireworkExplosionComponent> makeTransportFirework(int color) {
        return Collections.singletonList(new FireworkExplosionComponent(FireworkExplosionComponent.Type.LARGE_BALL, IntList.of(color), IntList.of(), false, false));
    }

    // TODO setup to cancel particles
    public static final SimpleParticleType GATE_STAR = FabricParticleTypes.simple(false);
}
