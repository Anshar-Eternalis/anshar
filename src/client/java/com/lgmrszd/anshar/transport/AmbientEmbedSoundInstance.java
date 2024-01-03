package com.lgmrszd.anshar.transport;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class AmbientEmbedSoundInstance extends MovingSoundInstance {
    private final ClientPlayerEntity player;
    
    protected AmbientEmbedSoundInstance(ClientPlayerEntity player, SoundEvent soundEvent) {
        super(soundEvent, SoundCategory.AMBIENT, SoundInstance.createRandom());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.2f;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (this.player.isRemoved() || !PlayerTransportComponent.KEY.get(player).isInNetwork()) {
            this.setDone();
        }
    }
}
