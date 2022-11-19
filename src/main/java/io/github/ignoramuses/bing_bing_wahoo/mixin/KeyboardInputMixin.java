package io.github.ignoramuses.bing_bing_wahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.ignoramuses.bing_bing_wahoo.extensions.KeyboardInputExtensions;
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
	
	@Inject(at = @At("TAIL"), method = "tick")
	public void wahoo$tick(boolean slowDown, float f, CallbackInfo ci) {
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
