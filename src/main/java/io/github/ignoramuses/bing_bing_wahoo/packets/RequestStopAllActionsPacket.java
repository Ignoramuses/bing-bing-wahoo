package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class RequestStopAllActionsPacket {
	public static final ResourceLocation ID = BingBingWahoo.id("request_stop_all_actions");

	public static void send(ServerPlayer player) {
		ServerPlayNetworking.send(player, ID, PacketByteBufs.empty());
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) ->
				client.execute(RequestStopAllActionsPacket::stopAllActions)
		);
	}

	private static void stopAllActions() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null)
			player.stopAllActions();
	}
}
