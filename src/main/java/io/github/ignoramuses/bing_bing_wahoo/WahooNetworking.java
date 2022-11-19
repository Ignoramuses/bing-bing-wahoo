package io.github.ignoramuses.bing_bing_wahoo;

import io.github.ignoramuses.bing_bing_wahoo.packets.*;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;

import static io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo.id;
import static io.github.ignoramuses.bing_bing_wahoo.WahooCommands.*;

public class WahooNetworking {
	public static final ResourceLocation UPDATE_BOOLEAN_GAMERULE_PACKET = id("update_boolean_gamerule_packet");
	
	public static void init() {
		CapThrowPacket.init();
		GroundPoundPacket.init();
		UpdatePreviousJumpTypePacket.init();
		StartFallFlyPacket.init();
		UpdateBonkPacket.init();
		UpdateDivePacket.init();
		UpdateFlipStatePacket.init();
		UpdatePosePacket.init();
		UpdateSlidePacket.init();

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
