package io.github.ignoramuses.bing_bing_wahoo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.ignoramuses.bing_bing_wahoo.extensions.AbstractClientPlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.extensions.LocalPlayerExtensions;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public PlayerRendererMixin(EntityRendererProvider.Context ctx, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;getArmPose(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", shift = At.Shift.BEFORE), method = "setModelProperties")
	private void wahoo$setModelPoseWhileGroundPoundingAndSliding(AbstractClientPlayer player, CallbackInfo ci) {
		if (player instanceof LocalPlayerExtensions extendedPlayer && extendedPlayer.wahoo$groundPounding()) {
			getModel().crouching = true;
		}
	}

	@Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("HEAD"))
	private void wahoo$flip(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerExtensions ex && entity instanceof EntityAccessor access) {
			int ticksFlipping = ex.wahoo$ticksFlipping();
			if (ticksFlipping != 0) {
				float yaw = entity.getYRot();
				Vec3 lookVec = access.wahoo$calculateViewVector(0, yaw - 90);
				Vector3f vec = new Vector3f(lookVec);
				int mult = ex.wahoo$flippingForwards() ? 1 : -1;
				if (entity instanceof LocalPlayer) { // some stuff is reversed locally.
					partialTicks = -partialTicks;
					mult = -mult;
				}
				vec.set(mult * vec.x(), 0, mult * vec.z());
				Quaternion q = vec.rotationDegrees((ticksFlipping + partialTicks) * 24); // magical speed number
				matrixStack.mulPose(q);
				matrixStack.translate(0, -0.9, 0); // roughly half the player's height
			}
		}
	}
}
