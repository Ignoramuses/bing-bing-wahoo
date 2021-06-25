package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient.JumpTypes;
import net.ignoramuses.bingBingWahoo.KeyboardInputExtensions;
import net.minecraft.block.Blocks;
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
	@Unique
	private final boolean wahoo$jumpHeldSinceLastJump = false;
	@Unique
	private final long wahoo$ticksSinceLaunch = 0;
	@Unique
	private final long wahoo$tickJumpedAt = 0;
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
	private long wahoo$tripleJumpTicks = 0;
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
	@Unique
	private long wahoo$ticksLeftToWallJump = 0;
	@Unique
	private boolean wahoo$colliding = false;
	@Unique
	private long wahoo$collisionHeartbeat = 0;
	@Unique
	private long wahoo$tickCollisionHeartbeat = 0;
	@Unique
	private boolean wahoo$hasCrouched = false;
	@Unique
	private boolean wahoo$incipientGroundPound = false;
	@Unique
	private long wahoo$ticksInAirDuringGroundPound = 0;
	@Unique
	private double wahoo$groundPoundSpeedMultiplier = 1.0;
	@Unique
	private boolean wahoo$walljumping = false;
	@Unique
	private boolean wahoo$isGroundPounding = false;
	@Unique
	private long wahoo$timeOnGround = 0;
	@Unique
	private boolean wahoo$hasGroundPounded = false;
	@Unique
	private long wahoo$blockBreakBuffer = 0;
	
	private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}
	
	@Shadow
	public abstract boolean isSneaking();
	
	@Shadow
	public abstract float getPitch(float tickDelta);
	
	@Shadow
	protected abstract boolean isWalking();

	/**
	 * Handles most tick-based physics, and when stuff should happen
	 */
	@Inject(at = @At("RETURN"), method = "tickMovement()V")
	public void wahoo$tickMovement(CallbackInfo ci) {
		updateJumpTicks();
		// Triple Jump Shenanigans
		if (wahoo$midTripleJump) {
			wahoo$tripleJumpTicks++;
			// number is actually irrelevant, is handled in our override
			if ((isOnGround() || !world.getFluidState(getBlockPos()).isEmpty()) && wahoo$tripleJumpTicks > 3) {
				exitTripleJump();
			}
			setPitch(0);
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
			
			if (!world.getFluidState(getBlockPos()).isEmpty()) {
				exitDive();
			}
			
			if (horizontalCollision) {
				bonk();
			}
		}
		
		// Initiates Diving
		if ((isSprinting()) && MinecraftClient.getInstance().options.keyAttack.isPressed() && !wahoo$isDiving && wahoo$previousJumpType != JumpTypes.LONG) {
			dive();
			wahoo$previousJumpType = JumpTypes.DIVE;
		}
		
		if (wahoo$bonked) {
			if (getPose() != EntityPose.SLEEPING) {
				setPose(EntityPose.SLEEPING);
			}
			setVelocity(getVelocity().multiply(0.8, 1, 0.8));
			--wahoo$bonkTime;
			if (wahoo$bonkTime == 0 || !world.getFluidState(getBlockPos()).isEmpty()) {
				setVelocity(0, getVelocity().getY(), 0);
				exitBonk();
			}
		}
		
		if (horizontalCollision) {
			if (wahoo$ticksLeftToWallJump <= 0 && wahoo$previousJumpType.canWallJumpFrom() && !wahoo$isDiving && !isOnGround()) {
				wahoo$ticksLeftToWallJump = 20;
			}
		}
		
		if (wahoo$ticksLeftToWallJump > 0 && wahoo$previousJumpType.canWallJumpFrom() && !wahoo$isDiving && input.jumping) {
			wallJump();
		}
		
		if (wahoo$walljumping && isOnGround()) {
			exitWallJump();
		}
		
		// Ground Pound Shenanigans
		if (isSneaking()) {
			wahoo$hasCrouched = true;
		}
		
		if (isOnGround() && !isSneaking()) {
			wahoo$hasCrouched = false;
		}
		
		if (!isOnGround() && wahoo$hasCrouched) {
			groundPound();
		}
		
		if (wahoo$isGroundPounding) {
			wahoo$hasGroundPounded = true;
			wahoo$ticksInAirDuringGroundPound++;
			if (wahoo$incipientGroundPound) {
				setVelocity(0, 0, 0);
			}
			
			if (wahoo$flipDegrees > 360) {
				wahoo$incipientGroundPound = false;
			}
			
			if (!wahoo$incipientGroundPound) {
				if (wahoo$ticksInAirDuringGroundPound > 6) {
					setVelocity(0, -0.5  * wahoo$groundPoundSpeedMultiplier , 0);
					if (wahoo$groundPoundSpeedMultiplier < 4) {
						wahoo$groundPoundSpeedMultiplier = wahoo$groundPoundSpeedMultiplier + 0.5;
					}
				}
			}
			
			if (world.getBlockState(getBlockPos().down()).getHardness(world, getBlockPos().down()) <= 0.5) {
				wahoo$blockBreakBuffer++;
				if (wahoo$blockBreakBuffer >= 2) {
					world.setBlockState(getBlockPos().down(), Blocks.AIR.getDefaultState());
					wahoo$blockBreakBuffer = 0;
				}
			} else if (isOnGround() && wahoo$hasGroundPounded) {
				exitGroundPound();
			}
		}
	}
	
	/**
	 * Handles triggering of jump-based physics
	 */
	@Override
	public void jump() {
		if (wahoo$isDiving) {
			addVelocity(0, 0.5, 0);
			wahoo$diveFlip = true;
			wahoo$flipDegrees = 0;
			setVelocity(getVelocity().multiply(1.25, 1, 1.25));
			return;
		}
		
		if (wahoo$bonked) {
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
	
	/**
	 * Keeps track of timers for jumps
	 */
	public void updateJumpTicks() {
		// double jump
		if (!lastOnGround && isOnGround()) {
			wahoo$ticksLeftToDoubleJump = 6;
		}
		if (wahoo$ticksLeftToDoubleJump > 0) {
			wahoo$ticksLeftToDoubleJump--;
		}
		
		// triple jump
		if (!lastOnGround && isOnGround()) {
			wahoo$ticksLeftToTripleJump = 6;
		}
		if (wahoo$ticksLeftToTripleJump > 0) {
			wahoo$ticksLeftToTripleJump--;
		}
		
		// long jump
		if (isSneaking() != lastSneaking) {
			wahoo$ticksSinceSneakingChanged = 0;
		}
		wahoo$ticksSinceSneakingChanged++;
		if (wahoo$ticksSinceSneakingChanged == 1 && isSneaking()) {
			wahoo$ticksLeftToLongJump = 5;
		}
		if (this.wahoo$ticksLeftToLongJump > 0) {
			wahoo$ticksLeftToLongJump--;
		}
		
		// wall jump
		if (wahoo$ticksLeftToWallJump > 0) {
			wahoo$ticksLeftToWallJump--;
			
			if (wahoo$ticksLeftToWallJump == 0 && wahoo$previousJumpType != JumpTypes.NORMAL && !wahoo$walljumping) {
				bonk();
			}
		}
	}
	
	/**
	 * An override of updatePose to allow for custom handling, sleeping for bonking and swimming for diving, etc.
	 */
	@Override
	protected void updatePose() {
		if (wahoo$isDiving || wahoo$bonked || wahoo$isGroundPounding) {
			return;
		}
		super.updatePose();
	}
	
	public void stackVelocity(double deltaX, double deltaY, double deltaZ) {
		setVelocity(Math.copySign(Math.abs(getVelocity().getX()) + deltaX, getVelocity().getX()), Math.copySign(Math.abs(getVelocity().getY()) + deltaY, getVelocity().getY()), Math.copySign(Math.abs(getVelocity().getZ()) + deltaZ, getVelocity().getZ()));
	}
	
	/**
	 * Similar to {@link ClientPlayerEntityMixin#updatePose}, allows for special handling of pitch changes
	 */
	@Override
	public void setPitch(float pitch) {
		if (wahoo$midTripleJump) {
			((EntityAccessor) this).setPitchRaw(getPitch() + 3);
			if (wahoo$isDiving || wahoo$isGroundPounding) {
				exitTripleJump();
			}
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
		
		if (wahoo$bonked) {
			return;
		}
		
		if (wahoo$isGroundPounding && wahoo$incipientGroundPound) {
			((EntityAccessor) this).setPitchRaw(getPitch() + 3);
			wahoo$flipDegrees += 3;
			return;
		}
		
		super.setPitch(pitch);
	}
	
	/**
	 * Similar to {@link ClientPlayerEntityMixin#updatePose}, allows for special handling of yaw changes
	 */
	public void setYaw(float yaw) {
		if (wahoo$bonked) {
			return;
		}
		super.setYaw(yaw);
	}
	
	// ---------- JUMPS ----------
	
	public void longJump() {
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
		
		double newVelXAbs;
		double newVelZAbs;
		
		if (degreesDiff > 170) { //BLJ
			newVelXAbs = Math.abs(getVelocity().getX()) * LONG_JUMP_SPEED_MULTIPLIER;
			newVelZAbs = Math.abs(getVelocity().getZ()) * LONG_JUMP_SPEED_MULTIPLIER;
		} else {
			newVelXAbs = Math.min(Math.abs(getVelocity().getX()) * LONG_JUMP_SPEED_MULTIPLIER, MAX_LONG_JUMP_SPEED);
			newVelZAbs = Math.min(Math.abs(getVelocity().getZ()) * LONG_JUMP_SPEED_MULTIPLIER, MAX_LONG_JUMP_SPEED);
		}
		
		double newVelX = Math.copySign(newVelXAbs, getVelocity().getX());
		double newVelZ = Math.copySign(newVelZAbs, getVelocity().getZ());
		// ----------- end of black magic wizardry -----------
		setVelocity(newVelX, Math.min(getVelocity().getY() * 1.25, 1), newVelZ);
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
	
	public void exitTripleJump() {
		wahoo$midTripleJump = false;
		wahoo$tripleJumpTicks = 0;
	}
	
	private void dive() {
		exitTripleJump();
		setPos(getPos().getX(), getPos().getY() + 1, getPos().getZ());
		wahoo$isDiving = true;
		setPose(EntityPose.SWIMMING);
		wahoo$currentDivingVelocity = getVelocity().multiply(2.25, 1, 2.25);
		setVelocity(wahoo$currentDivingVelocity);
		addVelocity(0, 0.5, 0);
	}
	
	public void exitDive() {
		wahoo$isDiving = false;
		wahoo$diveFlip = false;
		wahoo$flipDegrees = 0;
		setPitch(0);
	}
	
	public void bonk() {
		((KeyboardInputExtensions) input).disableControl();
		exitDive();
		exitTripleJump();
		setVelocity(-getVelocity().getX(), getVelocity().getY(), -getVelocity().getZ());
		setPose(EntityPose.SLEEPING);
		setPitch(-90);
		wahoo$bonked = true;
		wahoo$bonkTime = 30;
	}
	
	public void exitBonk() {
		((KeyboardInputExtensions) input).enableControl();
		wahoo$bonked = false;
		wahoo$bonkTime = 0;
		setPitch(0);
	}
	
	private void wallJump() {
		exitTripleJump();
		setRotation(-getYaw(), getPitch());
		setVelocity(-getVelocity().getX(), 0.5, -getVelocity().getZ());
		wahoo$walljumping = true;
		wahoo$ticksLeftToWallJump = 0;
		wahoo$previousJumpType = JumpTypes.WALL;
	}
	
	private void exitWallJump() {
		wahoo$walljumping = false;
	}
	
	private void groundPound() {
		wahoo$isGroundPounding = true;
		wahoo$incipientGroundPound = true;
	}
	
	private void exitGroundPound() {
		wahoo$isGroundPounding = false;
		wahoo$hasCrouched = false;
		wahoo$hasGroundPounded = false;
		wahoo$ticksInAirDuringGroundPound = 0;
		wahoo$flipDegrees = 0;
		wahoo$timeOnGround = 0; // might need to delete this variable
	}
}
