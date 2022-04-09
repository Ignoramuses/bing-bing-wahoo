package net.ignoramuses.bingBingWahoo;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ignoramuses.bingBingWahoo.WahooUtils.ServerPlayerExtensions;
import net.ignoramuses.bingBingWahoo.cap.CapPickupType;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.ignoramuses.bingBingWahoo.cap.PreferredCapSlot;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.ignoramuses.bingBingWahoo.movement.JumpTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

import java.util.UUID;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.id;
import static net.ignoramuses.bingBingWahoo.WahooCommands.*;

public class WahooNetworking {
	public static final ResourceLocation JUMP_TYPE_PACKET = id("jump_type_packet");
	public static final ResourceLocation GROUND_POUND_PACKET = id("ground_pound_packet");
	public static final ResourceLocation DIVE_PACKET = id("dive_packet");
	public static final ResourceLocation SLIDE_PACKET = id("slide_packet");
	public static final ResourceLocation CAP_THROW = id("cap_throw");
	public static final ResourceLocation UPDATE_BOOLEAN_GAMERULE_PACKET = id("update_boolean_gamerule_packet");
	public static final ResourceLocation CAP_ENTITY_SPAWN = id("cap_entity_spawn");
	public static final ResourceLocation UPDATE_PICKUP_TYPE = id("update_pickup_type");
	
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(UPDATE_PICKUP_TYPE, (server, player, handler, buf, responseSender) -> {
			CapPickupType type = CapPickupType.values()[buf.readVarInt()];
			server.execute(() -> BingBingWahoo.PLAYERS_TO_TYPES.put(player.getStringUUID(), type));
		});
		ServerPlayNetworking.registerGlobalReceiver(JUMP_TYPE_PACKET, (server, player, handler, buf, responseSender) -> {
			JumpTypes jumpType = JumpTypes.fromBuf(buf);
			server.execute(() -> ((ServerPlayerExtensions) player).wahoo$setPreviousJumpType(jumpType));
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean groundPounding = buf.readBoolean();
			boolean destruction = buf.readBoolean();
			server.execute(() -> ((ServerPlayerExtensions) player).wahoo$setGroundPounding(groundPounding, destruction));
		});
		ServerPlayNetworking.registerGlobalReceiver(DIVE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			BlockPos startPos = null;
			if (start) {
				startPos = buf.readBlockPos();
			}
			BlockPos finalStartPos = startPos;
			server.execute(() -> ((ServerPlayerExtensions) player).wahoo$setDiving(start, finalStartPos));
		});
		ServerPlayNetworking.registerGlobalReceiver(SLIDE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			server.execute(() -> ((ServerPlayerExtensions) player).wahoo$setSliding(start));
		});
		ServerPlayNetworking.registerGlobalReceiver(CAP_THROW, (server, player, handler, buf, responseSender) -> {
			boolean fromTrinketSlot = buf.readBoolean();
			server.execute(() -> {
				ItemStack cap = fromTrinketSlot ? TrinketsHandler.getCapStack(player) : player.getItemBySlot(EquipmentSlot.HEAD);
				FlyingCapEntity.spawn(player, cap, fromTrinketSlot ? PreferredCapSlot.TRINKETS : PreferredCapSlot.HEAD);
			});
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			GameRules rules = server.getGameRules();
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, writeBooleanRule(DESTRUCTIVE_GROUND_POUND_RULE, rules));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, writeBooleanRule(BACKWARDS_LONG_JUMPS_RULE, rules));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, writeBooleanRule(RAPID_FIRE_LONG_JUMPS_RULE, rules));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, writeBooleanRule(HAT_REQUIRED_RULE, rules));
			sender.sendPacket(UPDATE_DOUBLE_GAMERULE_PACKET, writeDoubleRule(MAX_LONG_JUMP_SPEED_RULE, rules));
			sender.sendPacket(UPDATE_DOUBLE_GAMERULE_PACKET, writeDoubleRule(LONG_JUMP_SPEED_MULTIPLIER_RULE, rules));
		});
	}

	public static FriendlyByteBuf writeBooleanRule(GameRules.Key<GameRules.BooleanValue> key, GameRules rules) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(key.getId()).writeBoolean(rules.getBoolean(key));
		return buf;
	}

	public static FriendlyByteBuf writeDoubleRule(GameRules.Key<DoubleRule> key, GameRules rules) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(key.getId()).writeDouble(rules.getRule(key).get());
		return buf;
	}
}
