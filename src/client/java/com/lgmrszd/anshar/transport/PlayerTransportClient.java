package com.lgmrszd.anshar.transport;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import com.lgmrszd.anshar.ModResources;
import com.lgmrszd.anshar.beacon.BeaconNode;
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
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.random.Random;

public class PlayerTransportClient {

    // TODO: unstatic all of this, create a client whenever player enters network and destroy when it leaves
    private static final int TICKS_TO_JUMP = (int)(20f * 11.5);
    private static int gateTicks = 0;
    private static Random random = Random.create();
    private static boolean firstTick = true;
    private static SoundInstance jumpSound = new TransportJumpSoundInstance(random);
    private static WeakRef<ParticleManager> particleManager = new WeakRef<ParticleManager>(null);
    private static BeaconNode nearest = null;

    public static void tick(ClientWorld world) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        var transport = PlayerTransportComponent.KEY.get(player);
        if (transport.isInNetwork()) {
            // audio
            if (firstTick) {
                // TODO this should REALLY be a constructor
                firstTick = false;
                playSound(new AmbientEmbedSoundInstance(player, ModResources.EMBED_SPACE_AMBIENT_SOUND_EVENT));
                particleManager = new WeakRef<>(MinecraftClient.getInstance().particleManager);
                if (nearest != null) {
                    player.lookAt(EntityAnchor.EYES, nearest.getPos().toCenterPos());
                }
            }
            
            // update gate status
            if (player.input.pressingForward) {
                if (nearest == null && gateTicks == 0 && player.getWorld().getTime() % 10 == 0) {
                    // wait for a nearest to be set
                    playSound(jumpSound);
                    nearest = transport.getNearestLookedAt();
                }
            } else {
                stopSound(jumpSound);
                gateTicks = 0;
                nearest = null;
            }

            // draw nearest gate only if jumping, otherwise draw all
            if (nearest == null) {
                for (var node : transport.getJumpCandidates()) {
                    drawGate(player, node, false, gateTicks);
                }
                gateTicks = 0;
            } else {
                gateTicks++;
                drawGate(player, nearest, true, gateTicks);
            }

            // jump if we ready
            if (gateTicks >= TICKS_TO_JUMP) {
                // oh god this is getting bad please make this a real class im so sorry
                gateTicks = 0;
                nearest = null;
                ClientPlayNetworking.send(PlayerTransportComponent.JUMP_PACKET_ID, PacketByteBufs.empty());
                stopSound(jumpSound);
            }

        } else {
            stopSound(jumpSound);
            gateTicks = 0;
            firstTick = true;
        }
    }


    private static final float GATE_PLAYER_DISTANCE = 20;
    private static final float BASE_GATE_HEIGHT = 10;
    private static final int BASE_PARTICLE_COUNT = 3;
    private static final float GATE_OPENING_COEF = 3f;
    private static final float PARTICLE_VEL_COEF = .1f;
    private static void drawGate(ClientPlayerEntity player, BeaconNode node, boolean nearest, int ticks) {
        // TODO replace some of this with making gates particle emitters/shaders
        // get vector from player to node pos
        var transport = PlayerTransportComponent.KEY.get(player);
        Vector3f normalGirl = transport.compassNormToNode(node);
        
        float jumpRatio = (float)ticks / (float)TICKS_TO_JUMP;
        float intensity = !nearest ? 1f : 1f + jumpRatio;

        Matrix3f M = new Matrix3f().setLookAlong(normalGirl, new Vector3f(0, 1, 0)).invert();
        Vector3f normalExt = normalGirl.mul(GATE_PLAYER_DISTANCE, new Vector3f());
        float gateWidth = GATE_OPENING_COEF * (intensity-1);
        float gateHeight = BASE_GATE_HEIGHT * (float)Math.pow(intensity, 4);
        float starSpeed = PARTICLE_VEL_COEF * ((float)Math.pow(intensity, 4)-1);

        if (nearest) {
            System.out.println("(" + node.getPos() + ") :: ticks: "+ gateTicks + ", ratio: "+ jumpRatio + ", width: " + gateWidth + ", speed: " + starSpeed);
        }
        
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < BASE_PARTICLE_COUNT * (int)Math.pow(intensity, 6); i++) {
                float pY = random.nextFloat() * gateHeight - gateHeight / 2;
                // should  probably do translate with a matrix4f instead
                Vector3f pPos = new Vector3f(gateWidth * side, pY, 0f).mul(M).add(normalExt).add(player.getEyePos().toVector3f());
                Vector3f pVel = new Vector3f(0, 0, starSpeed).mul(M);

                // why am I not using a shader at this point tbh? pathetic! die scoundrel! villain!
                particleManager.ifPresent(manager -> {
                    var particle = manager.addParticle(TransportEffects.GATE_STAR, pPos.x, pPos.y, pPos.z, pVel.x, pVel.y, pVel.z);
                    float[] colors = node.getColor();
                    if (nearest) {
                        particle.setColor(
                            colors[0] + jumpRatio * (random.nextFloat() - colors[0]), 
                            colors[1] + jumpRatio * (random.nextFloat() - colors[1]), 
                            colors[2] + jumpRatio * (random.nextFloat() - colors[2])
                        );
                    } else {
                        particle.setColor(colors[0], colors[1], colors[2]);
                    }
                });
            }
        }
    }

    public static float getJumpPercentage() { return (float)gateTicks / (float)TICKS_TO_JUMP; }

    public static void acceptExplosionPacketS2C(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (client.player == null) return;
        var pos = buf.readBlockPos().toCenterPos();
        client.execute(() -> {
            // TODO This has opposite effect of not showing effect when landing, so I commented it out :/
            // we really should delay sending the packet by like two ticks
            // Alternatively if the problem doesn't happen when exiting the network, we can just remove this
            // (As I made it not create the effect when entering for the player who enters)
//            var playerPos = MinecraftClient.getInstance().player.getPos();
//            if (!playerPos.isInRange(pos, PlayerTransportComponent.EXPLOSION_MAX_DISTANCE)) return;
            handler.getWorld().addFireworkParticle(pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, TransportEffects.TRANSPORT_EXPLOSION_FIREWORK);
        });
    }

    private static void playSound(SoundInstance sound) {
        MinecraftClient.getInstance().getSoundManager().play(sound);
    }

    private static void stopSound(SoundInstance sound) {
        MinecraftClient.getInstance().getSoundManager().stop(sound);
    }
}
