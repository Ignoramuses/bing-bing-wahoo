package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.KeyboardInputExtensions;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input implements KeyboardInputExtensions {
	@Unique
	private boolean wahoo$stopWASD = false;
	
	@Inject(at = @At("TAIL"), method = "tick(Z)V")
	public void tick(boolean slowDown, CallbackInfo ci) {
		if (wahoo$stopWASD) {
			pressingForward = false;
			pressingBack = false;
			pressingLeft = false;
			pressingRight = false;
			movementForward = 0;
			movementSideways = 0;
		}
	}
	
	@Override
	public void setStopWASD(boolean stop) {
		wahoo$stopWASD = stop;
	}
	
	@Override
	public boolean getStopWASD() {
		return wahoo$stopWASD;
	}
}
