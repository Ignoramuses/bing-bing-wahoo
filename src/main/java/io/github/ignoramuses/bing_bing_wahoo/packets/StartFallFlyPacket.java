package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class StartFallFlyPacket {
	public static final ResourceLocation ID = BingBingWahoo.id("start_fall_fly");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) ->
				server.execute(player::tryToStartFallFlying)
		);
	}

	@Environment(EnvType.CLIENT)
	public static void send() {
		ClientPlayNetworking.send(ID, PacketByteBufs.create());
	}
}
