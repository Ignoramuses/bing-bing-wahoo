package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.WahooNetworking;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;

public class CapSpawnPacket {
	public static final ResourceLocation ID = BingBingWahoo.id("cap_entity_spawn");

	public static Packet<?> makePacket(FlyingCapEntity entity) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		new ClientboundAddEntityPacket(entity).write(buf);
		CompoundTag data = new CompoundTag();
		entity.addAdditionalSaveData(data);
		buf.writeNbt(data);
		return ServerPlayNetworking.createS2CPacket(ID, buf);
	}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, sender) -> {
			ClientboundAddEntityPacket addPacket = new ClientboundAddEntityPacket(buf);
			CompoundTag data = buf.readNbt();
			client.execute(() -> {
				addPacket.handle(handler);
				FlyingCapEntity cap = (FlyingCapEntity) client.level.getEntity(addPacket.getId());
				if (cap != null) {
					cap.readAdditionalSaveData(data);
				}
			});
		});
	}
}
