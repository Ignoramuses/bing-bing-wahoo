package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class UpdateSlidePacket {
	public static final ResourceLocation ID = BingBingWahoo.id("update_slide");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			boolean start = buf.readBoolean();
			server.execute(() -> player.setSliding(start));
		});
	}

	@Environment(EnvType.CLIENT)
	public static void send(boolean start) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(start);
		ClientPlayNetworking.send(ID, buf);
	}
}
