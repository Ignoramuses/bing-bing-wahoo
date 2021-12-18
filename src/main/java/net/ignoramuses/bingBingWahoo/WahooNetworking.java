package net.ignoramuses.bingBingWahoo;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ignoramuses.bingBingWahoo.movement.JumpTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.ID;
import static net.ignoramuses.bingBingWahoo.WahooCommands.*;

public class WahooNetworking {
	public static final Identifier JUMP_TYPE_PACKET = new Identifier(ID, "jump_type_packet");
	public static final Identifier GROUND_POUND_PACKET = new Identifier(ID, "ground_pound_packet");
	public static final Identifier DIVE_PACKET = new Identifier(ID, "dive_packet");
	public static final Identifier SLIDE_PACKET = new Identifier(ID, "slide_packet");
	public static final Identifier BONK_PACKET = new Identifier(ID, "bonk_packet");
	public static final Identifier UPDATE_BOOLEAN_GAMERULE_PACKET = new Identifier(ID, "update_boolean_gamerule_packet");
	
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(JUMP_TYPE_PACKET, (server, player, handler, buf, responseSender) -> {
			JumpTypes jumpType = JumpTypes.fromBuf(buf);
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).setPreviousJumpType(jumpType));
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean groundPounding = buf.readBoolean();
			boolean destruction = buf.readBoolean();
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).setGroundPounding(groundPounding, destruction));
		});
		ServerPlayNetworking.registerGlobalReceiver(DIVE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			BlockPos startPos = null;
			if (start) {
				startPos = buf.readBlockPos();
			}
			BlockPos finalStartPos = startPos;
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).setDiving(start, finalStartPos));
		});
		ServerPlayNetworking.registerGlobalReceiver(SLIDE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).setSliding(start));
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
//					ServerPlayNetworking.send(tracker, BONK_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(start)).writeUuid(senderUUID));
//				}
//			});
//		});
	}
}