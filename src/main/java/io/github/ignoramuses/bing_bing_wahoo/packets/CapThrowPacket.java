package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.compat.TrinketsCompat;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapEntity;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.PreferredCapSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class CapThrowPacket {
	public static final ResourceLocation ID = BingBingWahoo.id("cap_throw");

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			boolean fromTrinketSlot = buf.readBoolean();
			server.execute(() -> {
				ItemStack cap = fromTrinketSlot ? TrinketsCompat.getCapTrinketStack(player) : player.getItemBySlot(EquipmentSlot.HEAD);
				FlyingCapEntity.spawn(player, cap, fromTrinketSlot ? PreferredCapSlot.TRINKETS : PreferredCapSlot.HEAD);
			});
		});
	}

	@Environment(EnvType.CLIENT)
	public static void send(boolean thrownFromTrinketSlot) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(thrownFromTrinketSlot);
		ClientPlayNetworking.send(ID, buf);
	}
}
