package com.lgmrszd.anshar.beacon;

import static com.lgmrszd.anshar.Anshar.LOGGER;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

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
}
