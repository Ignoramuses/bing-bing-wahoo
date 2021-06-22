package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient.JumpTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.ignoramuses.bingBingWahoo.BingBingWahooClient.LONG_JUMP_SPEED_MULTIPLIER;
import static net.ignoramuses.bingBingWahoo.BingBingWahooClient.MAX_LONG_JUMP_SPEED;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	@Shadow
	public Input input;
	@Shadow
	private boolean lastOnGround;
	@Shadow
	private boolean lastSneaking;
	@Unique
	private long wahoo$ticksSinceSneakingChanged = 0;
	@Unique
	private long wahoo$ticksLeftToLongJump = 0;
	@Unique
	private long wahoo$ticksLeftToDoubleJump = 0;
	@Unique
	private long wahoo$ticksLeftToTripleJump = 0;
	@Unique
	private JumpTypes wahoo$previousJumpType = JumpTypes.NORMAL;
	@Unique
	private boolean wahoo$midTripleJump = false;
	@Unique
	private boolean wahoo$isDiving = false;
	@Unique
	private Vec3d wahoo$currentDivingVelocity;
	@Unique
	private boolean wahoo$bonked = false;
	@Unique
	private long wahoo$bonkTime = 0;
	@Unique
	private boolean wahoo$diveFlip = false;
	@Unique
	private int wahoo$flipDegrees = 0;
	private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Shadow
	public abstract boolean isSneaking();

	@Shadow
	public abstract float getPitch(float tickDelta);

	@Shadow
	protected abstract boolean isWalking();
	
	@Shadow public abstract boolean isSubmergedInWater();
	
	@Inject(at = @At("RETURN"), method = "tickMovement()V")
	public void wahoo$tickMovement(CallbackInfo ci) {
		updateJumpTicks();
		// Triple Jump Shenanigans
		if (wahoo$midTripleJump) {
			if (isOnGround() || isTouchingWater()) {
				wahoo$midTripleJump = false;
				setPitch(0);
			} else {
				setPitch(0); // number is actually irrelevant, is handled in our override
			}
		}
		
		// Diving Shenanigans
		if (wahoo$isDiving) {
			if (wahoo$diveFlip) {
				setPitch(0);
			}
			
			if (isOnGround()) {
				Vec3d divingVelocity = new Vec3d(wahoo$currentDivingVelocity.getX() * 0.9, 0, wahoo$currentDivingVelocity.getZ() * 0.9);
				setVelocity(divingVelocity);
				wahoo$currentDivingVelocity = divingVelocity;
				if ((divingVelocity.getX() < 0.01 && divingVelocity.getX() > -0.01) && (divingVelocity.getZ() < 0.01 && divingVelocity.getZ() > -0.01)) {
					exitDive();
				}
			} else {
				setVelocity(wahoo$currentDivingVelocity.getX(), getVelocity().getY() - 0.05, wahoo$currentDivingVelocity.getZ());
			}
			
			if (isTouchingWater()) {
				exitDive();
			}
		}
		
		// Initiates Diving
		if ((isSprinting()) && MinecraftClient.getInstance().options.keyAttack.isPressed() && !wahoo$isDiving && wahoo$previousJumpType != JumpTypes.LONG) {
			dive();
			wahoo$previousJumpType = JumpTypes.DIVE;
		}
		
		// Thyne Bonking Shenanigans
		for (Direction direction : BingBingWahooClient.CARDINAL_DIRECTIONS) {
			if (direction != Direction.fromRotation(getYaw())) {
				continue;
			}
			
			if ((!world.getBlockState(getBlockPos().offset(direction)).isAir() ||// feet pos 1 block forwards in look direction
					!world.getBlockState(getBlockPos().offset(direction).up()).isAir()) // head pos 1 block forwards in look direction
					&& (wahoo$isDiving || (!isOnGround() && wahoo$previousJumpType != JumpTypes.NORMAL))) {
				bonk();
			}
		}

		if (wahoo$bonked) {
			multiplyHorizontalVelocity(0.8);
			--wahoo$bonkTime;
			if (wahoo$bonkTime < 0 || !world.getFluidState(getBlockPos()).isEmpty()) {
				wahoo$bonked = false;
			}
		}
	}
	
	public void exitDive() {
		wahoo$isDiving = false;
		wahoo$diveFlip = false;
		wahoo$flipDegrees = 0;
		setPitch(0);
	}
	
	@Override
	protected void updatePose() {
		if (wahoo$isDiving || wahoo$bonked) {
			return;
		}
		super.updatePose();
	}
	
	@Override
	public void setPitch(float pitch) {
		if (wahoo$midTripleJump) {
			((EntityAccessor) this).setPitchRaw(getPitch() + 3);
			return;
		}
		
		if (wahoo$diveFlip) {
			if (wahoo$flipDegrees > 360) {
				exitDive();
			}
			
			((EntityAccessor) this).setPitchRaw(getPitch() + 3);
			wahoo$flipDegrees += 3;
			return;
		}
		
		super.setPitch(pitch);
	}
	
	@Override
	public void jump() {
		if (wahoo$isDiving) {
			addVelocity(0, 0.5, 0);
			wahoo$diveFlip = true;
			wahoo$flipDegrees = 0;
			multiplyHorizontalVelocity(1.25);
			return;
		}
		super.jump();
		if (input.jumping) {
			if ((isOnGround())) {
				if ((isSneaking() || lastSneaking) && (BingBingWahooClient.rapidFire || wahoo$ticksLeftToLongJump > 0) && (wahoo$previousJumpType == JumpTypes.NORMAL || wahoo$previousJumpType == JumpTypes.LONG)) {
					longJump();
				} else if (wahoo$ticksLeftToDoubleJump > 0 && wahoo$ticksLeftToDoubleJump < 5 && wahoo$previousJumpType == JumpTypes.NORMAL) {
					doubleJump();
				} else if (wahoo$ticksLeftToTripleJump > 0 && wahoo$ticksLeftToTripleJump < 5 && wahoo$previousJumpType == JumpTypes.DOUBLE && (isSprinting() || isWalking())) {
					tripleJump();
				} else {
					wahoo$previousJumpType = JumpTypes.NORMAL;
				}
			}
		}
	}
	
	private Vec3d multiplyHorizontalVelocity(double multiplier) {
		double velX = getVelocity().getX();
		double velY = getVelocity().getY();
		double velZ = getVelocity().getZ();
		
		double velXAbs = Math.abs(velX);
		double velZAbs = Math.abs(velZ);
		
		double newVelXAbs;
		double newVelZAbs;
		
		double xToZRatio = velXAbs / velZAbs;
		
		// after re-reading this code, how does this work at all???
		
		// special handling for axis
		if (velXAbs == 0 || velZAbs == 0) {
			newVelXAbs = velXAbs * multiplier;
			newVelZAbs = velZAbs * multiplier;
		} else {
			newVelXAbs = velZAbs * multiplier * xToZRatio;
			newVelZAbs = velZAbs * multiplier;
		}
		
		double newVelX = Math.copySign(newVelXAbs, velX);
		double newVelZ = Math.copySign(newVelZAbs, velZ);
		
		setVelocity(newVelX, velY, newVelZ);
		return new Vec3d(newVelX, velY, newVelZ);
	}
	
	private void longJump() {
		// ------- warning: black magic wizardry below -------
		Vec2f velocity = new Vec2f((float) getVelocity().getX(), (float) getVelocity().getZ());
		Vec2f rotation = new Vec2f((float) getRotationVector().getX(), (float) getRotationVector().getZ());
		
		double cosOfVecs = (rotation.dot(velocity)) / (rotation.length() * velocity.length());
		double degreesDiff = Math.toDegrees(Math.acos(cosOfVecs));
		
		if (degreesDiff > 85 && degreesDiff < 95) { // don't long jump for moving straight left or right
			wahoo$ticksLeftToLongJump = 0;
			wahoo$previousJumpType = JumpTypes.NORMAL;
			return;
		}
		
		double velX = getVelocity().getX();
		double velY = getVelocity().getY();
		double velZ = getVelocity().getZ();
		
		double velXAbs = Math.abs(velX);
		double velZAbs = Math.abs(velZ);
		
		double newVelXAbs;
		double newVelZAbs;
		
		// after re-reading this code, how does this work at all???
		
		if (degreesDiff > 170) {
			newVelXAbs = velXAbs * LONG_JUMP_SPEED_MULTIPLIER;
			newVelZAbs = velZAbs * LONG_JUMP_SPEED_MULTIPLIER;
		} else {
			newVelXAbs = Math.min(velXAbs * LONG_JUMP_SPEED_MULTIPLIER, MAX_LONG_JUMP_SPEED);
			newVelZAbs = Math.min(velZAbs * LONG_JUMP_SPEED_MULTIPLIER, MAX_LONG_JUMP_SPEED);
		}
		
		double newVelX = Math.copySign(newVelXAbs, velX);
		double newVelZ = Math.copySign(newVelZAbs, velZ);
		
		setVelocity(newVelX, Math.min(velY * 1.25, 1), newVelZ);
		// ----------- end of black magic wizardry -----------
		wahoo$ticksLeftToLongJump = 0;
		wahoo$previousJumpType = JumpTypes.LONG;
	}
	
	private void doubleJump() {
		setVelocity(getVelocity().multiply(1, 1.75, 1));
		wahoo$ticksLeftToDoubleJump = 0;
		wahoo$previousJumpType = JumpTypes.DOUBLE;
	}
	
	private void tripleJump() {
		setVelocity(getVelocity().multiply(1, 2.5, 1));
		wahoo$ticksLeftToTripleJump = 0;
		wahoo$previousJumpType = JumpTypes.TRIPLE;
		wahoo$midTripleJump = true;
	}
	
	private void dive() {
		setPos(getPos().getX(), getPos().getY() + 1, getPos().getZ());
		wahoo$isDiving = true;
		setPose(EntityPose.SWIMMING);
		wahoo$currentDivingVelocity = multiplyHorizontalVelocity(2.25);
		addVelocity(0, 0.5, 0);
	}
	
	private void bonk() {
//		double velX = getVelocity().getX();
//		double velY = getVelocity().getY();
//		double velZ = getVelocity().getZ();
//
//		double newVelX = Math.copySign(1, velX);
//		double newVelZ = Math.copySign(1, velZ);
//
//		setVelocity(-newVelX, velY, -newVelZ);
//		setPose(EntityPose.SLEEPING);
//		wahoo$bonked = true;
//		wahoo$bonkTime = 60;
	}
	
	private void updateJumpTicks() {
		// double jump
		if (!lastOnGround && isOnGround()) {
			wahoo$ticksLeftToDoubleJump = 6;
		}
		if (wahoo$ticksLeftToDoubleJump > 0) {
			--this.wahoo$ticksLeftToDoubleJump;
		}
		
		// triple jump
		if (!lastOnGround && isOnGround()) {
			wahoo$ticksLeftToTripleJump = 6;
		}
		if (wahoo$ticksLeftToTripleJump > 0) {
			--this.wahoo$ticksLeftToTripleJump;
		}
		
		// long jump
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
