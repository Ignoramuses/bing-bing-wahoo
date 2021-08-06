package net.ignoramuses.bingBingWahoo;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.ignoramuses.bingBingWahoo.mixin.EntitySelectorOptionsAccessor;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BingBingWahoo implements ModInitializer {
	public static final Direction[] CARDINAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	public static final String ID = "bingbingwahoo";
	public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
	public static final Tag<Block> SLIDES = TagRegistry.block(new Identifier(ID, "slides"));
	public static final ArmorMaterial MYSTERIOUS_CAP_MATERIAL = new MysteriousCapArmorMaterial();
	public static DyeableArmorItem MYSTERIOUS_CAP;
	public static Item MUSIC_DISC_SLIDER;
	public static final Identifier SLIDER_ID = new Identifier(ID, "music_disc_slider");
	public static SoundEvent SLIDER;
	public static final Identifier JUMP_TYPE_PACKET = new Identifier(ID, "jump_type_packet");
	public static final Identifier GROUND_POUND_PACKET = new Identifier(ID, "ground_pound_packet");
	public static final Identifier DIVE_PACKET = new Identifier(ID, "dive_packet");
	public static final Identifier SLIDE_PACKET = new Identifier(ID, "slide_packet");
	public static final Identifier BONK_PACKET = new Identifier(ID, "bonk_packet");
	public static final Identifier UPDATE_BOOLEAN_GAMERULE_PACKET = new Identifier(ID, "update_boolean_gamerule_packet");
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
	public static final Identifier UPDATE_DOUBLE_GAMERULE_PACKET = new Identifier(ID, "update_double_gamerule_packet");
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
	
	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(JUMP_TYPE_PACKET, (server, player, handler, buf, responseSender) -> {
			JumpTypes jumpType = JumpTypes.fromBuf(buf);
			server.execute(() -> ((ServerPlayerEntityExtensions) player).setPreviousJumpType(jumpType));
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean groundPounding = buf.readBoolean();
			boolean destruction = buf.readBoolean();
			server.execute(() -> ((ServerPlayerEntityExtensions) player).setGroundPounding(groundPounding, destruction));
		});
		ServerPlayNetworking.registerGlobalReceiver(DIVE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			BlockPos startPos = null;
			if (start) {
				startPos = buf.readBlockPos();
			}
			BlockPos finalStartPos = startPos;
			server.execute(() -> ((ServerPlayerEntityExtensions) player).setDiving(start, finalStartPos));
		});
		ServerPlayNetworking.registerGlobalReceiver(SLIDE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			server.execute(() -> ((ServerPlayerEntityExtensions) player).setSliding(start));
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeString(DESTRUCTIVE_GROUND_POUND_RULE.getName()).writeBoolean(server.getGameRules().getBoolean(DESTRUCTIVE_GROUND_POUND_RULE))));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeString(BACKWARDS_LONG_JUMPS_RULE.getName()).writeBoolean(server.getGameRules().getBoolean(BACKWARDS_LONG_JUMPS_RULE))));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeString(RAPID_FIRE_LONG_JUMPS_RULE.getName()).writeBoolean(server.getGameRules().getBoolean(RAPID_FIRE_LONG_JUMPS_RULE))));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeString(HAT_REQUIRED_RULE.getName()).writeBoolean(server.getGameRules().getBoolean(HAT_REQUIRED_RULE))));
			sender.sendPacket(UPDATE_DOUBLE_GAMERULE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeString(MAX_LONG_JUMP_SPEED_RULE.getName()).writeDouble(server.getGameRules().get(MAX_LONG_JUMP_SPEED_RULE).get())));
			sender.sendPacket(UPDATE_DOUBLE_GAMERULE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeString(LONG_JUMP_SPEED_MULTIPLIER_RULE.getName()).writeDouble(server.getGameRules().get(LONG_JUMP_SPEED_MULTIPLIER_RULE).get())));
		});

//		ServerPlayNetworking.registerGlobalReceiver(BONK_PACKET, (server, player, handler, buf, responseSender) -> {
//			boolean start = buf.readBoolean();
//			UUID senderUUID = buf.readUuid();
//			server.execute(() -> {
//				for (ServerPlayerEntity tracker : PlayerLookup.tracking(player)) {
//					ServerPlayNetworking.send(tracker, BingBingWahoo.BONK_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(start)).writeUuid(senderUUID));
//				}
//			});
//		});
		
		MYSTERIOUS_CAP = Registry.register(Registry.ITEM, new Identifier(ID, "mysterious_cap"),
				new DyeableArmorItem(MYSTERIOUS_CAP_MATERIAL, EquipmentSlot.HEAD,
						new FabricItemSettings().rarity(Rarity.RARE).maxDamage(128).group(ItemGroup.MISC)));
		SLIDER = Registry.register(Registry.SOUND_EVENT, SLIDER_ID, new SoundEvent(SLIDER_ID));
		MUSIC_DISC_SLIDER = Registry.register(Registry.ITEM, new Identifier(ID, "music_disc_slider"), new MusicDiscItem(14, SLIDER, (new Item.Settings()).maxCount(1).group(ItemGroup.MISC).rarity(Rarity.RARE)){});
		
		EntitySelectorOptionsAccessor.invokePutOption("sliding", reader -> {
			boolean sliding = reader.getReader().readBoolean();
			reader.setIncludesNonPlayers(false);
			reader.setLocalWorldOnly();
			reader.setSuggestionProvider((suggestionsBuilder, suggestionsBuilderConsumer) -> CommandSource.suggestMatching(new String[]{"true", "false"}, suggestionsBuilder));
			reader.setPredicate(entity -> {
				if (entity instanceof PlayerEntityExtensions extendedPlayer) {
					return extendedPlayer.getSliding() == sliding;
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
									((ServerPlayerEntityExtensions) target).setDestructionPermOverride(BoolArgumentType.getBool(context, "value"));
									return 0;
								})))));
	}
}
