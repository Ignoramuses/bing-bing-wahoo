package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient.JumpTypes;
import net.ignoramuses.bingBingWahoo.ClientPlayerEntityExtensions;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements ClientPlayerEntityExtensions {
	@Shadow
	public Input input;
	@Shadow
	private boolean lastOnGround;
	@Shadow
	private boolean lastSneaking;
	
	private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}
	
	@Shadow
	public abstract boolean isSneaking();
	
	@Unique
	private long wahoo$ticksSinceSneakingChanged = 0;
	@Unique
	private long wahoo$ticksLeftToLongJump = 0;
	@Unique
	private long wahoo$ticksLeftToDoubleJump = 0;
	@Unique
	private long wahoo$ticksLeftToTripleJump = 0;
	@Unique
	private boolean wahoo$isLongJumping = false;
	@Unique
	private long wahoo$tickCount = 0;
	@Unique
	private JumpTypes previousJumpType;
	
	@Inject(at = @At("RETURN"), method = "tickMovement()V")
	public void wahoo$tickMovement(CallbackInfo ci) {
		wahoo$tickCount++;
		
		// long jumps
		updateSneakTicks();
		if (input.jumping && (isSneaking() || lastSneaking) && (onGround || lastOnGround)) {
			if (BingBingWahooClient.rapidFire) {
				longJump();
			} else if (wahoo$ticksLeftToLongJump > 0) {
				longJump();
				wahoo$ticksLeftToLongJump = 0;
			}
		}
		
		// Double Jump Code
		updateDoubleJumpTicks();
		if (input.jumping && (lastOnGround || isOnGround()) && (wahoo$ticksLeftToDoubleJump > 0)) {
			doubleJump();
		}
	}
	
	private void longJump() {
		setVelocity(getVelocity().multiply(10, 1.5, 10));
		wahoo$isLongJumping = true;
		previousJumpType = JumpTypes.LONG;
	}
	
	private void doubleJump() {
		setVelocity(getVelocity().multiply(1, 2.125, 1));
		wahoo$ticksLeftToDoubleJump = 0;
		previousJumpType = JumpTypes.DOUBLE;
	}
	
	private void updateDoubleJumpTicks() {
		if (!lastOnGround && isOnGround()) {
			wahoo$ticksLeftToDoubleJump = 5;
		}
		if (wahoo$ticksLeftToDoubleJump > 0) {
			--this.wahoo$ticksLeftToDoubleJump;
		}
	}
	
	private void updateSneakTicks() {
		if (isSneaking() != lastSneaking) {
			wahoo$ticksSinceSneakingChanged = 0;
		}
		++this.wahoo$ticksSinceSneakingChanged;
		if (wahoo$ticksSinceSneakingChanged == 1 && isSneaking()) {
			wahoo$ticksLeftToLongJump = 5;
		}
		if (this.wahoo$ticksLeftToLongJump > 0) {
			--this.wahoo$ticksLeftToLongJump;
		}
	}
}
