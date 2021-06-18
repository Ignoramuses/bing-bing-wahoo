package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.player.YeetProcessing;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Inject(at = @At("RETURN"), method = "tick()V")
	public void tick(CallbackInfo ci) {
		YeetProcessing.yeetPlayer((PlayerEntity) (Object) this);
	}
}
