package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient.JumpTypes;
import net.ignoramuses.bingBingWahoo.ClientPlayerEntityExtensions;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.ignoramuses.bingBingWahoo.BingBingWahooClient.MAX_LONG_JUMP_SPEED;

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
	private JumpTypes previousJumpType;
	
	@Inject(at = @At("RETURN"), method = "tickMovement()V")
	public void wahoo$tickMovement(CallbackInfo ci) {
		boolean jumpTypeSetThisTick = false;
		// long jumps
		updateSneakTicks();
		if (input.jumping && (isSneaking() || lastSneaking) && (onGround || lastOnGround) && previousJumpType != JumpTypes.TRIPLE) {
			if (BingBingWahooClient.rapidFire || wahoo$ticksLeftToLongJump > 0) {
				longJump();
				jumpTypeSetThisTick = true;
			}
		}
		
		// Double Jump Code
		updateDoubleJumpTicks();
		if (input.jumping && (lastOnGround || isOnGround()) && (wahoo$ticksLeftToDoubleJump > 0) && previousJumpType == JumpTypes.NORMAL) {
			doubleJump();
			jumpTypeSetThisTick = true;
		}
		
		if (isOnGround() && input.jumping && !jumpTypeSetThisTick) {
			previousJumpType = JumpTypes.NORMAL;
		}
	}
	
	private void longJump() {
		// ------- warning: black magic wizardry below -------
		Vec2f velocity = new Vec2f((float) getVelocity().getX(), (float) getVelocity().getZ());
		Vec2f rotation = new Vec2f((float) getRotationVector().getX(), (float) getRotationVector().getZ());
		
		double cosOfVecs = (rotation.dot(velocity)) / (rotation.length() * velocity.length());
		double degreesDiff = Math.toDegrees(Math.acos(cosOfVecs));
		
		
		double velX = getVelocity().getX();
		double velY = getVelocity().getY();
		double velZ = getVelocity().getZ();
		
		double velXAbs = Math.abs(velX);
		double velZAbs = Math.abs(velZ);
		
		double newVelXAbs = Math.min(velXAbs * 10, MAX_LONG_JUMP_SPEED.getX());
		double newVelZAbs = Math.min(velZAbs * 10, MAX_LONG_JUMP_SPEED.getZ());
		
		double newVelX = Math.copySign(newVelXAbs, velX);
		double newVelZ = Math.copySign(newVelZAbs, velZ);
		
		setVelocity(newVelX / 2, Math.min(velY * 1.5, MAX_LONG_JUMP_SPEED.getY()), newVelZ / 2);
		// ----------- end of black magic wizardry -----------
		wahoo$ticksLeftToLongJump = 0;
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
