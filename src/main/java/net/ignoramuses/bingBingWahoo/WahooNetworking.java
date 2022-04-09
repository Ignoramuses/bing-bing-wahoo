package net.ignoramuses.bingBingWahoo;

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

import java.util.UUID;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.id;
import static net.ignoramuses.bingBingWahoo.WahooCommands.*;

public class WahooNetworking {
	public static final ResourceLocation JUMP_TYPE_PACKET = id("jump_type_packet");
	public static final ResourceLocation GROUND_POUND_PACKET = id("ground_pound_packet");
	public static final ResourceLocation DIVE_PACKET = id("dive_packet");
	public static final ResourceLocation SLIDE_PACKET = id("slide_packet");
	public static final ResourceLocation BONK_PACKET = id("bonk_packet");
	public static final ResourceLocation DISABLE_IDENTITY_SWAPPING = id("disable_identity_swapping");
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
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeUtf(DISABLE_IDENTITY_SWAPPING_RULE.getId()).writeBoolean(server.getGameRules().getBoolean(DISABLE_IDENTITY_SWAPPING_RULE))));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeUtf(DESTRUCTIVE_GROUND_POUND_RULE.getId()).writeBoolean(server.getGameRules().getBoolean(DESTRUCTIVE_GROUND_POUND_RULE))));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeUtf(BACKWARDS_LONG_JUMPS_RULE.getId()).writeBoolean(server.getGameRules().getBoolean(BACKWARDS_LONG_JUMPS_RULE))));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeUtf(RAPID_FIRE_LONG_JUMPS_RULE.getId()).writeBoolean(server.getGameRules().getBoolean(RAPID_FIRE_LONG_JUMPS_RULE))));
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeUtf(HAT_REQUIRED_RULE.getId()).writeBoolean(server.getGameRules().getBoolean(HAT_REQUIRED_RULE))));
			sender.sendPacket(UPDATE_DOUBLE_GAMERULE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeUtf(MAX_LONG_JUMP_SPEED_RULE.getId()).writeDouble(server.getGameRules().getRule(MAX_LONG_JUMP_SPEED_RULE).get())));
			sender.sendPacket(UPDATE_DOUBLE_GAMERULE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeUtf(LONG_JUMP_SPEED_MULTIPLIER_RULE.getId()).writeDouble(server.getGameRules().getRule(LONG_JUMP_SPEED_MULTIPLIER_RULE).get())));
		});

//		ServerPlayNetworking.registerGlobalReceiver(BONK_PACKET, (server, player, handler, buf, responseSender) -> {
//			boolean start = buf.readBoolean();
//			UUID senderUUID = buf.readUuid();
//			server.execute(() -> {
//				for (ServerPlayerEntity tracker : PlayerLookup.tracking(player)) {
//					ServerPlayNetworking.send(tracker, BONK_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(start)).writeUuid(senderUUID));
//				}
//			});
//		});
	}
}
