package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;

public class UpdatePosePacket {
	public static final ResourceLocation ID = BingBingWahoo.id("update_pose");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, ((server, player, handler, buf, responseSender) -> {
			Pose newPose = Pose.values()[buf.readVarInt()];
			server.execute(() -> player.setPose(newPose));
		}));
	}

	@Environment(EnvType.CLIENT)
	public static void send(Pose requested) {
		int ordinal = requested.ordinal();
		FriendlyByteBuf buf = PacketByteBufs.create()
				.writeVarInt(ordinal);
		ClientPlayNetworking.send(ID, buf);
	}
}
