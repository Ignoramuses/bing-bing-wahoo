package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class BingBingWahoo implements ModInitializer {
	public static final Direction[] CARDINAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	public static final String ID = "bingbingwahoo";
	public static final Identifier JUMP_TYPE_PACKET = new Identifier(ID, "jump_type_packet");
	public static final Identifier GROUND_POUND_PACKET = new Identifier(ID, "ground_pound_packet");
	public static final Identifier DIVE_PACKET = new Identifier(ID, "dive_packet");
	public static final Identifier BONK_PACKET = new Identifier(ID, "bonk_packet");
	public static final ArmorMaterial MYSTERIOUS_CAP_MATERIAL = new MysteriousCapArmorMaterial();
	public static DyeableArmorItem MYSTERIOUS_CAP;
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
//		ServerPlayNetworking.registerGlobalReceiver(BONK_PACKET, (server, player, handler, buf, responseSender) -> {
//			boolean start = buf.readBoolean();
//			UUID senderUUID = buf.readUuid();
//			server.execute(() -> {
//				for (ServerPlayerEntity tracker : PlayerLookup.tracking(player)) {
//					ServerPlayNetworking.send(tracker, BingBingWahoo.BONK_PACKET, PacketByteBufs.duplicate(PacketByteBufs.create().writeBoolean(start)).writeUuid(senderUUID));
//				}
//			});
//		});
		
		MYSTERIOUS_CAP = Registry.register(Registry.ITEM, new Identifier(ID, "mysterious_cap"),
				new DyeableArmorItem(MYSTERIOUS_CAP_MATERIAL, EquipmentSlot.HEAD,
						new FabricItemSettings().rarity(Rarity.RARE).maxDamage(128).group(ItemGroup.MISC)));
	}
}
