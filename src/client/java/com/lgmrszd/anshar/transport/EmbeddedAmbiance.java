package com.lgmrszd.anshar.transport;

import com.lgmrszd.anshar.ModResources;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;

public class EmbeddedAmbiance extends MovingSoundInstance {
    private static final int TICKS_TO_PLAY_TUNES = 10 * 20;
    private static final int TICKS_TO_TRANSITION = 3 * 20;

    private final MinecraftClient client;
    private final ClientPlayerEntity player;

    private EmbeddedMusic music = null;
    private int ticksToPlay = TICKS_TO_PLAY_TUNES;

    
    protected EmbeddedAmbiance(MinecraftClient client) {
        super(ModResources.EMBED_SPACE_AMBIENT_SOUND_EVENT, SoundCategory.AMBIENT, SoundInstance.createRandom());
        this.client = client;
        this.player = client.player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.2f;
        this.relative = true;
    }

    private class EmbeddedMusic extends MovingSoundInstance {
        
        protected EmbeddedMusic() {
            super(ModResources.EMBED_SPACE_MUSIC_EVENT, SoundCategory.AMBIENT, EmbeddedAmbiance.this.random);
            this.repeat = false;
            this.repeatDelay = 0;
            this.volume = 1.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (EmbeddedAmbiance.this.isDone() || !client.getSoundManager().isPlaying(this)) {
                this.setDone();
            }
        }

    }

    @Override
    public void tick() {
        if (this.player.isRemoved() || !PlayerTransportComponent.KEY.get(player).isInNetwork()) {
            this.setDone();
        }

        --ticksToPlay;
        if (ticksToPlay < 0 && ticksToPlay > -TICKS_TO_TRANSITION) {
            this.volume = Math.max(0f, Math.min(1f, (float)ticksToPlay/(float)-TICKS_TO_TRANSITION));
        } else if (ticksToPlay < -TICKS_TO_TRANSITION) {
            if (this.music == null) {
                this.music = new EmbeddedMusic();
                this.client.getSoundManager().play(music);
            } else if (this.music.isDone()) {
                this.music = null;
            }
        }
    }
}
