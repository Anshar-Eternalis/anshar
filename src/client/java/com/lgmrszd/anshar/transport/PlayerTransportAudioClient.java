package com.lgmrszd.anshar.transport;

import java.util.HashMap;
import java.util.Map;

import com.lgmrszd.anshar.AnsharClient;
import com.lgmrszd.anshar.ModResources;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;

class PlayerTransportAudioClient {
    // state machine for managing a transport client's audio just to keep things tidy
    // if there's a better way to do this let me know

    // map of sound, isPlaying
    // calling multiple starts and stops in the same tick causes issues
    private final Map<SoundInstance, Boolean> sounds = new HashMap<>();

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final SoundInstance ambientSound;
    private SoundInstance jumpSound = null;

    protected PlayerTransportAudioClient() {
        ambientSound = new EmbeddedAmbiance(client);
    }

    public void playJump() {
        if (jumpSound != null) return;
        jumpSound = new AbstractSoundInstance(ModResources.TRANSPORT_JUMP_SOUND_EVENT, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {{
            this.volume = 0.2f;
            this.pitch = 1.0f;
            this.repeat = false;
            this.relative = false;
        }};
        client.getSoundManager().play(jumpSound);
    }

    public void stopJump() {
        if (jumpSound == null) return;
        client.getSoundManager().stop(jumpSound);
        jumpSound = null;
    }

    public void playAmbient() {
        client.getSoundManager().play(ambientSound);
    }
}
