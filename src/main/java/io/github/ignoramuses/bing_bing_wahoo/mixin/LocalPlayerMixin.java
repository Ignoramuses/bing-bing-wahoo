package io.github.ignoramuses.bing_bing_wahoo.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.behavior.WahooLogic;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends AbstractClientPlayer {
	@Unique
	private final WahooLogic logic = new WahooLogic((LocalPlayer) (Object) this);

	@Unique
	private boolean ticked = false;

	public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V"
			)
	)
	private void startTick(CallbackInfo ci) {
		this.ticked = true;
		logic.startTick();
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void endTick(CallbackInfo ci) {
		if (ticked) {
			logic.endTick();
		}
		this.ticked = false;
	}

	@Override
	@Intrinsic
	public void jumpFromGround() {
		super.jumpFromGround();
	}

	@Inject(method = "jumpFromGround()V", at = @At("TAIL"))
	private void onJump(CallbackInfo ci) {
		logic.afterJump();
	}

	@Override
	@Intrinsic
	protected float getJumpPower() {
		return super.getJumpPower();
	}

	@Inject(method = "getJumpPower()F", at = @At("RETURN"), cancellable = true)
	private void modifyJumpPower(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(cir.getReturnValueF() * logic.simpleJumps.powerMultiplierForNextJump());
	}
}
