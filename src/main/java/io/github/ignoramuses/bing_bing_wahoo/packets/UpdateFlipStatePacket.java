package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.FlipState;
import io.github.ignoramuses.bing_bing_wahoo.extensions.AbstractClientPlayerExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class UpdateFlipStatePacket {
	public static final ResourceLocation ID = BingBingWahoo.id("update_flip");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			FlipState state = buf.readEnum(FlipState.class);
			server.execute(() -> {
				UUID id = player.getGameProfile().getId();
				for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
					if (tracker != player) {
						FriendlyByteBuf buffer = PacketByteBufs.create();
						buffer.writeEnum(state);
						buffer.writeUUID(id);
						ServerPlayNetworking.send(tracker, ID, buffer);
					}
				}
			});
		});
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
			FlipState state = buf.readEnum(FlipState.class);
			UUID id = buf.readUUID();
			client.execute(() -> {
				for (AbstractClientPlayer player : client.level.players()) {
					if (player instanceof AbstractClientPlayerExtensions ex && player.getGameProfile().getId().equals(id)) {
						ex.setFlipState(state);
						break;
					}
				}
			});
		});
	}

	@Environment(EnvType.CLIENT)
	public static void send(FlipState newState) {
		FriendlyByteBuf buf = PacketByteBufs.create()
				.writeEnum(newState);
		ClientPlayNetworking.send(ID, buf);
	}
}
