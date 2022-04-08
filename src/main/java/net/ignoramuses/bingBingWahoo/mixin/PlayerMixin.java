package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.WahooUtils.PlayerExtensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerExtensions {
	@Unique
	private boolean wahoo$isBonked = false;

	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}
	
	@Override
	public void wahoo$setBonked(boolean value, UUID bonked) {
		wahoo$isBonked = value;
	}
	
	@Inject(at = @At("HEAD"), method = "updatePlayerPose", cancellable = true)
	protected void wahoo$updatePlayerPose(CallbackInfo ci) {
		if (wahoo$isBonked) {
			setPose(Pose.SLEEPING);
			ci.cancel();
		}
	}
	
	@Override
	public void setPose(Pose pose) {
		if (wahoo$isBonked) {
			super.setPose(Pose.SLEEPING);
			return;
		}
		super.setPose(pose);
	}
}
