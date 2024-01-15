package com.lgmrszd.anshar;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.beacon.EndCrystalItemContainer;
import com.lgmrszd.anshar.config.ServerConfig;
import com.lgmrszd.anshar.dispenser.ModDispenserBehaviors;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.neoforged.fml.config.ModConfig;

public class ModRegistration {
    public static void registerAll() {
        ModApi.register();

        ModDispenserBehaviors.register();

        ModGroup.register();

        NeoForgeConfigRegistry.INSTANCE.register(Anshar.MOD_ID, ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC);


        ServerPlayNetworking.registerGlobalReceiver(PlayerTransportComponent.JUMP_PACKET_ID, 
            (server, player, b, packet, d) -> server.execute(() -> PlayerTransportComponent.KEY.get(player).tryJump(BeaconNode.fromNBT(packet.readNbt())))
        );

        ServerPlayNetworking.registerGlobalReceiver(BeaconComponent.ENTER_PACKET_ID,
                (server, player, b, packet, d) -> {
//                    NbtCompound nbt = packet.readNbt();
//                    if (nbt == null) return;
//                    BeaconNode node = BeaconNode.fromNBT(nbt);
                    BlockPos pos = packet.readBlockPos();
                    server.execute(() -> {
                        if (!(player.getWorld().getBlockEntity(pos) instanceof BeaconBlockEntity bbe)) return;
                        BeaconComponent.KEY.get(bbe).getFrequencyNetwork().ifPresent(frequencyNetwork ->
                                PlayerTransportComponent.KEY.get(player).enterNetwork(frequencyNetwork, pos)
                        );
                    });
//                    UUID freqUUID = packet.readUuid();
//                    server.execute(() -> {
//                        NetworkManagerComponent.KEY.get(player.getWorld().getLevelProperties()).getNetwork(freqUUID).ifPresent(frequencyNetwork -> {
//                            PlayerTransportComponent.KEY.get(player).enterNetwork(frequencyNetwork, pos);
//                        });
//                    });
                }
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
