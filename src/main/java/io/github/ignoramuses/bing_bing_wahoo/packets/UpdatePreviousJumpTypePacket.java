package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.JumpType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class UpdatePreviousJumpTypePacket {
	public static final ResourceLocation ID = BingBingWahoo.id("jump_type_packet");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			JumpType jumpType = buf.readEnum(JumpType.class);
			server.execute(() -> player.wahoo$setPreviousJumpType(jumpType));
		});
	}

	@Environment(EnvType.CLIENT)
	public static void send(JumpType previous) {
		FriendlyByteBuf buf = PacketByteBufs.create()
				.writeEnum(previous);
		ClientPlayNetworking.send(ID, buf);
	}
}
