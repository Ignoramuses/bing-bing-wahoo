package net.ignoramuses.bingBingWahoo;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ignoramuses.bingBingWahoo.mixin.EntitySelectorOptionsAccessor;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

import java.util.List;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.ID;
import static net.ignoramuses.bingBingWahoo.WahooNetworking.UPDATE_BOOLEAN_GAMERULE_PACKET;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WahooCommands {
	public static GameRules.Key<GameRules.BooleanRule> DISABLE_IDENTITY_SWAPPING_RULE = GameRuleRegistry.register("disableIdentitySwapping", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		PacketByteBuf buffer = new PacketByteBuf(PacketByteBufs.create().writeString("disableIdentitySwapping").writeBoolean(rule.get()));
		for (ServerPlayerEntity player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<GameRules.BooleanRule> DESTRUCTIVE_GROUND_POUND_RULE = GameRuleRegistry.register("destructiveGroundPounds", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		PacketByteBuf buffer = new PacketByteBuf(PacketByteBufs.create().writeString("destructiveGroundPounds").writeBoolean(rule.get()));
		for (ServerPlayerEntity player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<GameRules.BooleanRule> BACKWARDS_LONG_JUMPS_RULE = GameRuleRegistry.register("backwardsLongJumps", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		PacketByteBuf buffer = new PacketByteBuf(PacketByteBufs.create().writeString("backwardsLongJumps").writeBoolean(rule.get()));
		for (ServerPlayerEntity player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<GameRules.BooleanRule> RAPID_FIRE_LONG_JUMPS_RULE = GameRuleRegistry.register("rapidFireLongJumps", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		PacketByteBuf buffer = new PacketByteBuf(PacketByteBufs.create().writeString("rapidFireLongJumps").writeBoolean(rule.get()));
		for (ServerPlayerEntity player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<GameRules.BooleanRule> HAT_REQUIRED_RULE = GameRuleRegistry.register("mysteriousCapRequired", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) -> {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		PacketByteBuf buffer = new PacketByteBuf(PacketByteBufs.create().writeString("mysteriousCapRequired").writeBoolean(rule.get()));
		for (ServerPlayerEntity player : players) {
			ServerPlayNetworking.send(player, UPDATE_BOOLEAN_GAMERULE_PACKET, buffer);
		}
	}));
	public static final Identifier UPDATE_DOUBLE_GAMERULE_PACKET = BingBingWahoo.id("update_double_gamerule_packet");
	public static GameRules.Key<DoubleRule> MAX_LONG_JUMP_SPEED_RULE = GameRuleRegistry.register("longJumpMaxSpeed", GameRules.Category.PLAYER, GameRuleFactory.createDoubleRule(1.5, (server, rule) -> {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		PacketByteBuf buffer = new PacketByteBuf(PacketByteBufs.create().writeString("longJumpMaxSpeed").writeDouble(rule.get()));
		for (ServerPlayerEntity player : players) {
			ServerPlayNetworking.send(player, UPDATE_DOUBLE_GAMERULE_PACKET, buffer);
		}
	}));
	public static GameRules.Key<DoubleRule> LONG_JUMP_SPEED_MULTIPLIER_RULE = GameRuleRegistry.register("longJumpSpeedMultiplier", GameRules.Category.PLAYER, GameRuleFactory.createDoubleRule(10, (server, rule) -> {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		PacketByteBuf buffer = new PacketByteBuf(PacketByteBufs.create().writeString("longJumpSpeedMultiplier").writeDouble(rule.get()));
		for (ServerPlayerEntity player : players) {
			ServerPlayNetworking.send(player, UPDATE_DOUBLE_GAMERULE_PACKET, buffer);
		}
	}));
	
	public static void init() {
		EntitySelectorOptionsAccessor.wahoo$invokePutOption("sliding", reader -> {
			boolean sliding = reader.getReader().readBoolean();
			reader.setIncludesNonPlayers(false);
			reader.setLocalWorldOnly();
			reader.setSuggestionProvider((suggestionsBuilder, suggestionsBuilderConsumer) -> CommandSource.suggestMatching(new String[]{"true", "false"}, suggestionsBuilder));
			reader.setPredicate(entity -> {
				if (entity instanceof WahooUtils.PlayerEntityExtensions extendedPlayer) {
					return extendedPlayer.wahoo$getSliding() == sliding;
				}
				return false;
			});
		}, entitySelectorReader -> true, new TranslatableText("argument.entity.options.sliding.description"));
		
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(literal("bingbingwahoo:setDestructionPerms")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("target", EntityArgumentType.player())
						.then(argument("value", BoolArgumentType.bool())
								.executes(context -> {
									ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
									((WahooUtils.ServerPlayerEntityExtensions) target).wahoo$setDestructionPermOverride(BoolArgumentType.getBool(context, "value"));
									return 0;
								})))));
		
	}
}
