package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class UpdateBonkPacket {
	public static final ResourceLocation ID = BingBingWahoo.id("update_bonk");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			boolean started = buf.readBoolean();
			server.execute(() ->
					player.wahoo$setBonked(started));
		});
	}

	@Environment(EnvType.CLIENT)
	public static void send(boolean started) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(started);
		ClientPlayNetworking.send(ID, buf);
	}
}
