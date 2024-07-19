package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.beacon.EndCrystalItemContainer;
import com.lgmrszd.anshar.config.ServerConfig;
import com.lgmrszd.anshar.dispenser.ModDispenserBehaviors;
import com.lgmrszd.anshar.payload.c2s.EnterPayload;
import com.lgmrszd.anshar.payload.c2s.JumpPayload;
import com.lgmrszd.anshar.payload.s2c.ExplosionPayload;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.neoforged.fml.config.ModConfig;

public class ModRegistration {
    public static void registerAll() {
        ModApi.register();

        ModComponentTypes.register();

        ModDispenserBehaviors.register();

        ModGroup.register();

        NeoForgeConfigRegistry.INSTANCE.register(Anshar.MOD_ID, ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC);

        PayloadTypeRegistry.playC2S().register(EnterPayload.ID, EnterPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JumpPayload.ID, JumpPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ExplosionPayload.ID, ExplosionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(JumpPayload.ID, 
            (payload, ctx) -> ctx.server().execute(() -> PlayerTransportComponent.KEY.get(ctx.player()).tryJump(BeaconNode.fromNBT(payload.nbt(), ctx.server().getRegistryManager())))
        );

        ServerPlayNetworking.registerGlobalReceiver(EnterPayload.ID,
                BeaconComponent::EnterBeamPacketC2S
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
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity serverPlayer)) return true;
            PlayerTransportComponent component = PlayerTransportComponent.KEY.get(serverPlayer);
            if (component.isInNetwork()) {
                component.exitNetwork();
            }
            return true;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            EndCrystalItemContainer container = ModApi.END_CRYSTAL_ITEM.find(player.getStackInHand(hand), null);
            if (container == null) return ActionResult.PASS;
            return container.onUse(player, world, hand, hitResult);
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!player.isSneaking() || !(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.pass(null);
            ItemStack stack = hand == Hand.MAIN_HAND ? player.getMainHandStack() : player.getOffHandStack();
            if (!stack.isOf(Items.END_CRYSTAL)) return TypedActionResult.pass(null);
            EndCrystalItemContainer container = ModApi.END_CRYSTAL_ITEM.find(stack, null);
            if (container == null || container.getBeaconPos().isEmpty()) return TypedActionResult.pass(null);
            container.clearBeaconPos(serverPlayer);
            return TypedActionResult.success(stack);
        });
    }

    private static void registerCommands() {
        ModCommands.register();
    }
}
