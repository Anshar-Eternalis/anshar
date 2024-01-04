package com.lgmrszd.anshar;

import com.lgmrszd.anshar.advancements.EnteredNetworkCriterion;
import com.lgmrszd.anshar.advancements.NetworkJumpCriterion;
import com.lgmrszd.anshar.config.ServerConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anshar implements ModInitializer {
	
	public static final String MOD_ID = "anshar";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static EnteredNetworkCriterion ENTERED_NETWORK = Criteria.register(MOD_ID + "/entered_network", new EnteredNetworkCriterion());
	public static NetworkJumpCriterion NETWORK_JUMP = Criteria.register(MOD_ID + "/network_jump", new NetworkJumpCriterion());
	public static final GameRules.Key<GameRules.IntRule> END_CRYSTAL_LINKING_DISTANCE =
			GameRuleRegistry.register(
					"ansharEndCrystalLinkingDistance",
					GameRules.Category.MISC,
					GameRuleFactory.createIntRule(32, 0, 64, (minecraftServer, intRule) -> {
						minecraftServer.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
							minecraftServer.execute(() -> {
								ServerConfig.sendNewValue(serverPlayer, intRule.get());
							});
						});
					})
			);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModRegistration.registerAll();
	}
}