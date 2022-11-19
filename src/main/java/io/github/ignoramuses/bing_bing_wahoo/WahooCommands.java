package io.github.ignoramuses.bing_bing_wahoo;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.ignoramuses.bing_bing_wahoo.extensions.PlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.extensions.ServerPlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.mixin.EntitySelectorOptionsAccessor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import java.util.List;

import static io.github.ignoramuses.bing_bing_wahoo.WahooNetworking.UPDATE_BOOLEAN_GAMERULE_PACKET;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WahooCommands {
	public static GameRules.Key<GameRules.BooleanValue> DESTRUCTIVE_GROUND_POUND_RULE = GameRuleRegistry.register("destructiveGroundPounds", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create().writeUtf("destructiveGroundPounds").writeBoolean(rule.get()));
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<GameRules.BooleanValue> BACKWARDS_LONG_JUMPS_RULE = GameRuleRegistry.register("backwardsLongJumps", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create().writeUtf("backwardsLongJumps").writeBoolean(rule.get()));
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<GameRules.BooleanValue> RAPID_FIRE_LONG_JUMPS_RULE = GameRuleRegistry.register("rapidFireLongJumps", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create().writeUtf("rapidFireLongJumps").writeBoolean(rule.get()));
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<GameRules.BooleanValue> HAT_REQUIRED_RULE = GameRuleRegistry.register("mysteriousCapRequired", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create().writeUtf("mysteriousCapRequired").writeBoolean(rule.get()));
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static final ResourceLocation UPDATE_DOUBLE_GAMERULE_PACKET = BingBingWahoo.id("update_double_gamerule_packet");
	public static GameRules.Key<DoubleRule> MAX_LONG_JUMP_SPEED_RULE = GameRuleRegistry.register("longJumpMaxSpeed", GameRules.Category.PLAYER, GameRuleFactory.createDoubleRule(1.5, (server, rule) -> {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create().writeUtf("longJumpMaxSpeed").writeDouble(rule.get()));
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, UPDATE_DOUBLE_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<DoubleRule> LONG_JUMP_SPEED_MULTIPLIER_RULE = GameRuleRegistry.register("longJumpSpeedMultiplier", GameRules.Category.PLAYER, GameRuleFactory.createDoubleRule(10, (server, rule) -> {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		FriendlyByteBuf buffer = new FriendlyByteBuf(PacketByteBufs.create().writeUtf("longJumpSpeedMultiplier").writeDouble(rule.get()));
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, UPDATE_DOUBLE_GAMERULE_PACKET, buffer);
		}
	}));
	
	public static void init() {
		EntitySelectorOptionsAccessor.wahoo$register("sliding", reader -> {
			boolean sliding = reader.getReader().readBoolean();
			reader.setIncludesEntities(false);
			reader.setWorldLimited();
			reader.setSuggestions((suggestionsBuilder, suggestionsBuilderConsumer) -> SharedSuggestionProvider.suggest(new String[]{"true", "false"}, suggestionsBuilder));
			reader.addPredicate(entity -> {
				if (entity instanceof PlayerExtensions extendedPlayer) {
					return extendedPlayer.wahoo$getSliding() == sliding;
				}
				return false;
			});
		}, entitySelectorReader -> true, Component.translatable("argument.entity.options.sliding.description"));
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(literal("bingbingwahoo:setDestructionPerms")
						.requires(source -> source.hasPermission(2))
						.then(argument("target", EntityArgument.player())
								.then(argument("value", BoolArgumentType.bool())
										.executes(context -> {
											ServerPlayer target = EntityArgument.getPlayer(context, "target");
											((ServerPlayerExtensions) target).wahoo$setDestructionPermOverride(BoolArgumentType.getBool(context, "value"));
											return 0;
										})
								)
						)
				)
		);
		
	}
}
