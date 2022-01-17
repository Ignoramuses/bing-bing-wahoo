package net.ignoramuses.bingBingWahoo;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ignoramuses.bingBingWahoo.WahooUtils.ServerPlayerEntityExtensions;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.ignoramuses.bingBingWahoo.cap.PreferredCapSlot;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.ignoramuses.bingBingWahoo.movement.JumpTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.ID;
import static net.ignoramuses.bingBingWahoo.WahooCommands.*;

public class WahooNetworking {
	public static final Identifier JUMP_TYPE_PACKET = BingBingWahoo.id("jump_type_packet");
	public static final Identifier GROUND_POUND_PACKET = BingBingWahoo.id("ground_pound_packet");
	public static final Identifier DIVE_PACKET = BingBingWahoo.id("dive_packet");
	public static final Identifier SLIDE_PACKET = BingBingWahoo.id("slide_packet");
	public static final Identifier BONK_PACKET = BingBingWahoo.id("bonk_packet");
	public static final Identifier DISABLE_IDENTITY_SWAPPING = BingBingWahoo.id("disable_identity_swapping");
	public static final Identifier CAP_THROW = BingBingWahoo.id("cap_throw");
	public static final Identifier IDENTITY_REQUEST_ADDON = BingBingWahoo.id("identity_request_addon");
	public static final Identifier UPDATE_BOOLEAN_GAMERULE_PACKET = BingBingWahoo.id("update_boolean_gamerule_packet");
	public static final Identifier CAP_ENTITY_SPAWN = BingBingWahoo.id("cap_entity_spawn");
	
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(JUMP_TYPE_PACKET, (server, player, handler, buf, responseSender) -> {
			JumpTypes jumpType = JumpTypes.fromBuf(buf);
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).wahoo$setPreviousJumpType(jumpType));
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean groundPounding = buf.readBoolean();
			boolean destruction = buf.readBoolean();
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).wahoo$setGroundPounding(groundPounding, destruction));
		});
		ServerPlayNetworking.registerGlobalReceiver(DIVE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			BlockPos startPos = null;
			if (start) {
				startPos = buf.readBlockPos();
			}
			BlockPos finalStartPos = startPos;
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).wahoo$setDiving(start, finalStartPos));
		});
		ServerPlayNetworking.registerGlobalReceiver(SLIDE_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			server.execute(() -> ((WahooUtils.ServerPlayerEntityExtensions) player).wahoo$setSliding(start));
		});
		ServerPlayNetworking.registerGlobalReceiver(CAP_THROW, (server, player, handler, buf, responseSender) -> {
			boolean fromTrinketSlot = buf.readBoolean();
			server.execute(() -> {
				ItemStack cap = fromTrinketSlot ? TrinketsHandler.getCapStack(player) : player.getEquippedStack(EquipmentSlot.HEAD);
				FlyingCapEntity.spawn(player, cap, fromTrinketSlot ? PreferredCapSlot.TRINKETS : PreferredCapSlot.HEAD);
				if (fromTrinketSlot) {
					TrinketsHandler.equipInHatSlot(player, ItemStack.EMPTY);
				} else {
					player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(IDENTITY_REQUEST_ADDON, (server, player, handler, buf, responseSender) -> server.execute(() -> {
			NbtCompound captured = ((ServerPlayerEntityExtensions) player).wahoo$getCaptured();
			if (captured != null) {
				String typeId = captured.getString("Type");
				NbtCompound entityData = captured.getCompound("Entity");
				EntityType<?> type = Registry.ENTITY_TYPE.get(new Identifier(typeId));
				Entity entity = type.create(player.world);
				if (entity != null) {
					entity.readNbt(entityData);
					player.world.spawnEntity(entity);
					player.world.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
				}
			}
		}));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPacket(UPDATE_BOOLEAN_GAMERULE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeString(DISABLE_IDENTITY_SWAPPING_RULE.getName()).writeBoolean(server.getGameRules().getBoolean(DISABLE_IDENTITY_SWAPPING_RULE))));
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
