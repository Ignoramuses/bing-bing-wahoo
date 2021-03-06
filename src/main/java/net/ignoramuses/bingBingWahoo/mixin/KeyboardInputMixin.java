package net.ignoramuses.bingBingWahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.WahooUtils;
import net.ignoramuses.bingBingWahoo.WahooUtils.KeyboardInputExtensions;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input implements KeyboardInputExtensions {
	@Unique
	private boolean wahoo$disableControl = false;
	
	@Inject(at = @At("TAIL"), method = "tick(Z)V")
	public void wahoo$tick(boolean slowDown, CallbackInfo ci) {
		if (wahoo$disableControl) {
			up = false;
			down = false;
			left = false;
			right = false;
			forwardImpulse = 0;
			leftImpulse = 0;
			jumping = false;
			shiftKeyDown = false;
		}
	}
	
	@Override
	public void wahoo$disableControl() {
		wahoo$disableControl = true;
	}
	
	@Override
	public void wahoo$enableControl() {
		wahoo$disableControl = false;
	}
}
