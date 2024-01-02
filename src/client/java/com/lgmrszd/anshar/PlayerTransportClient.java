package com.lgmrszd.anshar;

import org.joml.Vector3d;

import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.beacon.PlayerTransportComponent;
import com.lgmrszd.anshar.beacon.TransportEffects;
import com.lgmrszd.anshar.util.WeakRef;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class PlayerTransportClient {
    private static final int TICKS_TO_JUMP = 20 * 3;
    private static int gateTicks = 0;
    private static Random random = Random.create();
    private static boolean firstTick = true;
    private static SoundInstance jumpSound = new TransportJumpSoundInstance(random);
    private static WeakRef<ParticleManager> particleManager = new WeakRef<ParticleManager>(null);

    // helper text tracking
    private static int timeAtNode = 0;
    private static BeaconNode prevNode = null;

    public static void tick(ClientWorld world) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        var transport = PlayerTransportComponent.KEY.get(player);
        if (transport.isInNetwork()) {
            // audio
            if (firstTick) {
                firstTick = false;
                playSound(new AmbientEmbedSoundInstance(player, ModResources.EMBED_SPACE_AMBIENT_SOUND_EVENT));
                particleManager = new WeakRef<>(MinecraftClient.getInstance().particleManager);
            }

            if (gateTicks == 1) {
                playSound(jumpSound);
            }
            
            // update particles
            var nearest = transport.getNearestLookedAt();

            for (var node : transport.getJumpCandidates()) {
                // get vector from player to node pos
                var normalGirl = transport.normedVecToNode(node);
                var spos = normalGirl.multiply(10).add(player.getEyePos());

                int particleCount = 3;
                double intensity = 1;

                // draw gate
                if (nearest != null && node == nearest) {
                    intensity = Math.pow(intensity + 5 * gateTicks / TICKS_TO_JUMP, 2);
                }

                for (int i = 0; i < particleCount; i++) {
                    // get vector tangent to normal vector pointing in random radial direction
                    var ppos = getRotationAbout(new Vec3d(0, 1, 0), normalGirl, random.nextFloat() * 2 * Math.PI).multiply(intensity).add(spos);
                    var speed = ppos.subtract(spos).multiply(intensity);
                    particleManager.ifPresent(manager -> {
                        var particle = manager.addParticle(ParticleTypes.GLOW, ppos.getX(), ppos.getY(), ppos.getZ(), speed.x, speed.y, speed.z);
                        float[] colors = node.getColor();
                        particle.setColor(colors[0], colors[1], colors[2]);
                    });
                }
            }
            
            // update gate stuff
            if (player.input.pressingForward) {
                gateTicks += 1;
                if (gateTicks >= TICKS_TO_JUMP) {
                    gateTicks = 0;
                    ClientPlayNetworking.send(PlayerTransportComponent.JUMP_PACKET_ID, PacketByteBufs.empty());
                }
            } else {
                if (gateTicks > 0) {
                    stopSound(jumpSound);
                }
                gateTicks = 0;
            }
        } else {
            if (gateTicks > 0) {
                stopSound(jumpSound);
            }
            gateTicks = 0;
            firstTick = true;
        }
    }

    public static float getJumpPercentage() { return (float)gateTicks / (float)TICKS_TO_JUMP; }

    public static void acceptExplosionPacketS2C(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var pos = buf.readBlockPos().toCenterPos();
        client.execute(() -> handler.getWorld().addFireworkParticle(pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, TransportEffects.TRANSPORT_EXPLOSION_FIREWORK));
    }

    
    private static Vec3d getRotationAbout(Vec3d vec, Vec3d normalAxis, double radians) {
        var rot = new Vector3d(vec.x, vec.y, vec.z).rotateAxis(radians, normalAxis.x, normalAxis.y, normalAxis.z);
        return new Vec3d(rot.x, rot.y, rot.z);
    }

    private static void playSound(SoundInstance sound) {
        MinecraftClient.getInstance().getSoundManager().play(sound);
    }

    private static void stopSound(SoundInstance sound) {
        MinecraftClient.getInstance().getSoundManager().stop(sound);
    }
}
