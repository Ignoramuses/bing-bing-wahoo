package net.ignoramuses.bingBingWahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.WahooUtils;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input implements WahooUtils.KeyboardInputExtensions {
	@Unique
	private boolean wahoo$disableControl = false;
	
	@Inject(at = @At("TAIL"), method = "tick(Z)V")
	public void wahoo$tick(boolean slowDown, CallbackInfo ci) {
		if (wahoo$disableControl) {
			pressingForward = false;
			pressingBack = false;
			pressingLeft = false;
			pressingRight = false;
			movementForward = 0;
			movementSideways = 0;
			jumping = false;
			sneaking = false;
		}
	}
	
	@Override
	public void disableControl() {
		wahoo$disableControl = true;
	}
	
	@Override
	public void enableControl() {
		wahoo$disableControl = false;
	}
}
