package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.beacon.EndCrystalItemContainer;
import com.lgmrszd.anshar.config.ServerConfig;
import com.lgmrszd.anshar.dispenser.ModDispenserBehaviors;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class ModRegistration {
    public static void registerAll() {
        ModApi.register();

        ModDispenserBehaviors.register();

        ModGroup.register();

        ServerPlayNetworking.registerGlobalReceiver(PlayerTransportComponent.JUMP_PACKET_ID, 
            (server, player, b, packet, d) -> server.execute(() -> PlayerTransportComponent.KEY.get(player).tryJump(BeaconNode.fromNBT(packet.readNbt())))
        );

        Registry.register(Registries.SOUND_EVENT, ModResources.EMBED_SPACE_AMBIENT_SOUND, ModResources.EMBED_SPACE_AMBIENT_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, ModResources.TRANSPORT_JUMP_SOUND, ModResources.TRANSPORT_JUMP_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, ModResources.EMBED_SPACE_MUSIC, ModResources.EMBED_SPACE_MUSIC_EVENT);

        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Anshar.MOD_ID, "gate_star"), TransportEffects.GATE_STAR);

        registerCommands();
        registerEvents();
    }

    private static void registerEvents() {
//        DebugEvents.register();
        SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            ServerConfig.sendNewValue(
                    player,
                    player.getWorld().getGameRules().getInt(Anshar.END_CRYSTAL_LINKING_DISTANCE)
            );
        });
        ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity serverPlayer)) return true;
            PlayerTransportComponent component = PlayerTransportComponent.KEY.get(serverPlayer);
            if (component.isInNetwork()) {
                component.exitNetwork();
            }
            return true;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!player.isSneaking() || world.isClient()) return TypedActionResult.pass(null);
            ItemStack stack = hand == Hand.MAIN_HAND ? player.getMainHandStack() : player.getOffHandStack();
            if (!stack.isOf(Items.END_CRYSTAL)) return TypedActionResult.pass(null);
            EndCrystalItemContainer container = ModApi.END_CRYSTAL_ITEM.find(stack, null);
            if (container == null || container.getBeaconPos().isEmpty()) return TypedActionResult.pass(null);
            container.clearBeaconPos();
            player.sendMessage(Text.translatable("anshar.tooltip.end_crystal.use.unlinked"));
            return TypedActionResult.success(stack);
        });
    }

    private static void registerCommands() {
        ModCommands.register();
    }
}
