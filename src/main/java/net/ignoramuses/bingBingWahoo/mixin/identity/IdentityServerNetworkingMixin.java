package net.ignoramuses.bingBingWahoo.mixin.identity;

import draylar.identity.network.ServerNetworking;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ignoramuses.bingBingWahoo.WahooNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerNetworking.class)
public class IdentityServerNetworkingMixin {
	@Inject(method = "lambda$registerIdentityRequestPacketHandler$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;calculateDimensions()V"))
	private static void wahoo$sendUncapturePacket(PacketContext context, EntityType type, CallbackInfo ci) {
		ServerPlayNetworking.send((ServerPlayerEntity) context.getPlayer(), WahooNetworking.CAPTURE, PacketByteBufs.create().writeNbt(null));
	}
}
