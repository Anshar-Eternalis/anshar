package com.lgmrszd.anshar.transport;

import com.lgmrszd.anshar.ModResources;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;

public class TransportJumpSoundInstance extends AbstractSoundInstance {

    protected TransportJumpSoundInstance(Random random) {
        super(ModResources.TRANSPORT_JUMP_SOUND_EVENT, SoundCategory.NEUTRAL, random);
        this.volume = 0.2f;
        this.pitch = 0.5f;
        this.repeat = false;
    }
    
}
