package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class BingBingWahoo implements ModInitializer {
	public static final Direction[] CARDINAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	public static final String ID = "bingbingwahoo";
	public static final Identifier JUMP_TYPE_PACKET = new Identifier(ID, "jump_type_packet");
	public static final Identifier GROUND_POUND_PACKET = new Identifier(ID, "ground_pound_packet");
	
	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(JUMP_TYPE_PACKET, (server, player, handler, buf, responseSender) -> {
			JumpTypes jumpType = JumpTypes.fromBuf(buf);
			server.execute(() -> ((ServerPlayerEntityExtensions) player).setPreviousJumpType(jumpType));
		});
		ServerPlayNetworking.registerGlobalReceiver(GROUND_POUND_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean groundPounding = buf.readBoolean();
			server.execute(() -> ((ServerPlayerEntityExtensions) player).setGroundPounding(groundPounding));
		});
	}
}
