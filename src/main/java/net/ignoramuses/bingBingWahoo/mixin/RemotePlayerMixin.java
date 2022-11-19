package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.extensions.AbstractClientPlayerExtensions;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerMixin implements AbstractClientPlayerExtensions {
	@Unique
	private int wahoo$ticksFlipping;
	@Unique
	private boolean wahoo$forwardsFlipping;

	@Inject(method = "tick", at = @At("TAIL"))
	private void wahoo$updateFlipCounter(CallbackInfo ci) {
		if (wahoo$ticksFlipping > 0) wahoo$ticksFlipping++;
	}

	@Override
	public int wahoo$ticksFlipping() {
		return wahoo$ticksFlipping;
	}

	@Override
	public void wahoo$setFlipping(boolean value) {
		wahoo$ticksFlipping = value ? 1 : 0;
	}

	@Override
	public void wahoo$setFlipDirection(boolean forwards) {
		wahoo$forwardsFlipping = forwards;
	}

	@Override
	public boolean wahoo$flippingForwards() {
		return wahoo$forwardsFlipping;
	}
}
