package com.lgmrszd.anshar;

import com.lgmrszd.anshar.frequency.NetworkManagerComponent;
import com.lgmrszd.anshar.storage.EmbeddedStorage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.*;

public class ModCommands {

    private static final SuggestionProvider<ServerCommandSource> NETWORK_UUID_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(() -> {
                ServerWorld world = context.getSource().getWorld();
                return NetworkManagerComponent.KEY.get(world.getLevelProperties()).getNetworks().stream().map(frequencyNetwork -> frequencyNetwork.getId().toString()).sorted().iterator();
            }, builder);
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(literal("anshar")
                        .then(literal("network")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(argument("Network ID", UuidArgumentType.uuid())
                                        .suggests(NETWORK_UUID_SUGGESTIONS)
                                        .executes(ModCommands::networkIDCommand)
                                        .then(literal("storage")
                                                .executes(ModCommands::networkIDStorageCommand)
                                        )
                                )
                                .then(literal("list")
                                        .executes(ModCommands::networkListCommand)
                                )
                        )
                )
        );
    }

    private static int networkIDCommand(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        final UUID networkID = UuidArgumentType.getUuid(context, "Network ID");
        return NetworkManagerComponent.KEY.get(world.getLevelProperties())
                .getNetwork(networkID)
                .map(frequencyNetwork -> {
                    context.getSource().sendFeedback(
                            () -> Text.literal(
                                    "Frequency Network with ID\n%s\nhas this many Item Stacks: %d"
                                            .formatted(
                                                    networkID.toString(),
                                                    frequencyNetwork
                                                            .getStorage()
                                                            .getHeldStacks()
                                                            .stream()
                                                            .filter(itemStack -> !itemStack.isEmpty())
                                                            .count()
                                            )
                            ),
                            false);
                    context.getSource().sendFeedback(() -> Text.literal("And has following Beacons:"),
                            false);
                    frequencyNetwork.getBeacons().forEach(pos -> {
                        String beacon_data = String.format("%s", pos.toString());
                        context.getSource().sendFeedback(() -> Text.literal(beacon_data),
                                false);

                    });
                    return 0;
                })
                .orElseGet(() -> {
                    context.getSource().sendFeedback(() -> Text.literal("Invalid Frequency Network with ID " + networkID.toString()),
                            false);
                    return -1;
                });
    }

    private static int networkIDStorageCommand(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return -1;
        final UUID networkID = UuidArgumentType.getUuid(context, "Network ID");
        return NetworkManagerComponent.KEY.get(world.getLevelProperties())
                .getNetwork(networkID)
                .map(frequencyNetwork -> {
                    EmbeddedStorage inventory = frequencyNetwork.getStorage();
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                            (syncId, playerInv, playerx) -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInv, inventory),
                            Text.literal("[")
                                    .append(Text.translatable("block.minecraft.beacon"))
                                    .append("] ")
                                    .append(Text.translatable("container.enderchest"))));
                    return 0;
                })
                .orElseGet(() -> {
                    context.getSource().sendFeedback(() -> Text.literal("Invalid Frequency Network with ID " + networkID.toString()),
                            false);
                    return -1;
                });
    }

    private static int networkListCommand(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        if (world == null) return -1;
        context.getSource().sendFeedback(() -> Text.literal("Frequency Networks:"),
                false);
        NetworkManagerComponent.KEY.get(world.getLevelProperties()).getNetworks().forEach(frequencyNetwork -> {
            String network_data = String.format(
                    "UUID: %s, Beacons: %d, Item Stacks stored: %d",
                    frequencyNetwork.getId(),
                    frequencyNetwork.getBeacons().size(),
                    frequencyNetwork
                            .getStorage()
                            .getHeldStacks()
                            .stream()
                            .filter(itemStack -> !itemStack.isEmpty())
                            .count()
            );
            context.getSource().sendFeedback(() -> Text.literal(network_data),
                    false);
        });
        return 0;
    }
}
