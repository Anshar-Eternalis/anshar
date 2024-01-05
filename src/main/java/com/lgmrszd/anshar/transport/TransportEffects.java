package com.lgmrszd.anshar.transport;

import static com.lgmrszd.anshar.Anshar.LOGGER;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.DefaultParticleType;

public class TransportEffects {
    public static final NbtCompound TRANSPORT_EXPLOSION_FIREWORK;
    static {
        NbtCompound transportExplosion = null;
        try {
            transportExplosion = StringNbtReader.parse("{Explosions:[{Type:1,Colors:[I;16777215]}]}");
        } catch (CommandSyntaxException lol) {
            LOGGER.error("Failed to create transport explosion effect!");
        } finally {
            TRANSPORT_EXPLOSION_FIREWORK = transportExplosion;
        }
    }

    // TODO setup to cancel particles
    public static final DefaultParticleType GATE_STAR = FabricParticleTypes.simple(false);
}
