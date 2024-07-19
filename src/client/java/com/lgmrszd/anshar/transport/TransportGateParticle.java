package com.lgmrszd.anshar.transport;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class TransportGateParticle extends SpriteBillboardParticle {
    
    protected TransportGateParticle(ClientWorld clientWorld, double px, double py, double pz, double vx, double vy, double vz,  SpriteProvider provider) {
        super(clientWorld, px, py, pz);
        this.velocityMultiplier = 1.1f;
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.setSprite(provider);
    }

    // copy [vanilla]
    public static class Factory
    implements ParticleFactory<SimpleParticleType> {

        @Override
        public Particle createParticle(SimpleParticleType defaultParticleType, ClientWorld clientWorld, 
        double px, double py, double pz, double vx, double vy, double vz) {
            TransportGateParticle gateParticle = new TransportGateParticle(clientWorld, px, py, pz, vx, vy, vz, this.spriteProvider);
            return gateParticle;
        }

        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getBrightness(float tint) {
        return 0xF000F0;
    }
    
}
