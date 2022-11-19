package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class UpdateDivePacket {
	public static final ResourceLocation ID = BingBingWahoo.id("update_dive");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			BlockPos startPos = null;
			if (start) {
				startPos = buf.readBlockPos();
			}
			BlockPos finalStartPos = startPos;
			server.execute(() -> player.wahoo$setDiving(start, finalStartPos));
		});
	}

	@Environment(EnvType.CLIENT)
	public static void sendStart(BlockPos startPos) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(true);
		buf.writeBlockPos(startPos);
		ClientPlayNetworking.send(ID, buf);
	}

	@Environment(EnvType.CLIENT)
	public static void sendStop() {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(false);
		ClientPlayNetworking.send(ID, buf);
	}
}
