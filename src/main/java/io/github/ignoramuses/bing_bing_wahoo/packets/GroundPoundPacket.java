package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.BingBingWahooConfig;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.GroundPoundType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class GroundPoundPacket {
	public static final ResourceLocation ID = BingBingWahoo.id("ground_pound_packet");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			boolean groundPounding = buf.readBoolean();
			boolean destruction = buf.readBoolean();
			server.execute(() -> player.wahoo$setGroundPounding(groundPounding, destruction));
		});
	}

	@Environment(EnvType.CLIENT)
	public static void send(boolean started) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(started);
		buf.writeBoolean(BingBingWahooConfig.groundPoundType == GroundPoundType.DESTRUCTIVE);
		ClientPlayNetworking.send(ID, buf);
	}
}
