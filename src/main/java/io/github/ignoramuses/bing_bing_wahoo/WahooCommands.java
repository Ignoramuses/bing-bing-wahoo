package io.github.ignoramuses.bing_bing_wahoo;

import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.ignoramuses.bing_bing_wahoo.packets.RequestStopAllActionsPacket;
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

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WahooCommands {
	public static void init() {
		EntitySelectorOptionsAccessor.callRegister("sliding", reader -> {
			boolean sliding = reader.getReader().readBoolean();
			reader.setIncludesEntities(false);
			reader.setWorldLimited();
			reader.setSuggestions((suggestionsBuilder, suggestionsBuilderConsumer) -> SharedSuggestionProvider.suggest(new String[]{"true", "false"}, suggestionsBuilder));
			reader.addPredicate(entity -> {
				if (entity instanceof PlayerExtensions extendedPlayer) {
					return extendedPlayer.getSliding() == sliding;
				}
				return false;
			});
		}, entitySelectorReader -> true, Component.translatable("argument.entity.options.sliding.description"));
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
					dispatcher.register(literal("bingbingwahoo:setDestructionPerms")
							.requires(source -> source.hasPermission(2))
							.then(argument("target", EntityArgument.player())
									.then(argument("value", BoolArgumentType.bool())
											.executes(context -> {
												ServerPlayer target = EntityArgument.getPlayer(context, "target");
												target.setDestructionPermOverride(BoolArgumentType.getBool(context, "value"));
												return 0;
											})
									)
							)
					);
					dispatcher.register(literal("bingbingwahoo:stopAllActions")
							.requires(source -> source.hasPermission(2))
							.then(argument("target", EntityArgument.player())
									.executes(ctx -> {
										ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
										RequestStopAllActionsPacket.send(player);
										return 1;
									})
							)
					);
				}
		);
		
	}
}
