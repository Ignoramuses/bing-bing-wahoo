package io.github.ignoramuses.bing_bing_wahoo.mixin;

import io.github.ignoramuses.bing_bing_wahoo.content.movement.FlipState;
import io.github.ignoramuses.bing_bing_wahoo.extensions.AbstractClientPlayerExtensions;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerMixin implements AbstractClientPlayerExtensions {
	@Unique
	private int ticksFlipping;
	@Unique
	private boolean forwardsFlipping;

	@Inject(method = "tick", at = @At("TAIL"))
	private void updateFlipCounter(CallbackInfo ci) {
		if (ticksFlipping > 0) ticksFlipping++;
	}

	@Override
	public int ticksFlipping() {
		return ticksFlipping;
	}

	@Override
	public void setFlipState(FlipState state) {
		ticksFlipping = state != FlipState.NONE ? 1 : 0;
		forwardsFlipping = state == FlipState.FORWARDS;
	}

	@Override
	public boolean flippingForwards() {
		return forwardsFlipping;
	}
}
