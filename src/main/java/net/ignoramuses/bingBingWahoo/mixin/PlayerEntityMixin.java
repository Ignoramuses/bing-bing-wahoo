package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.PlayerEntityExtensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityExtensions {
	@Unique
	private boolean wahoo$isBonked = false;
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}
	
	@Override
	public void setBonked(boolean value, UUID bonked) {
		wahoo$isBonked = value;
	}
	
	@Inject(at = @At("HEAD"), method = "updatePose()V", cancellable = true)
	protected void wahoo$updatePose(CallbackInfo ci) {
		if (wahoo$isBonked) {
			setPose(EntityPose.SLEEPING);
			ci.cancel();
		}
	}
	
	@Override
	public void setPose(EntityPose pose) {
		if (wahoo$isBonked) {
			super.setPose(EntityPose.SLEEPING);
			return;
		}
		super.setPose(pose);
	}
}
