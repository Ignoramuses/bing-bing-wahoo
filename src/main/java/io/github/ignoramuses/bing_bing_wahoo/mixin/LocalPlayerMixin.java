package io.github.ignoramuses.bing_bing_wahoo.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.FlipState;
import io.github.ignoramuses.bing_bing_wahoo.packets.*;
import io.github.ignoramuses.bing_bing_wahoo.synced_config.SyncedConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.ignoramuses.bing_bing_wahoo.*;
import io.github.ignoramuses.bing_bing_wahoo.compat.AutomobilityCompat;
import io.github.ignoramuses.bing_bing_wahoo.compat.TrinketsCompat;
import io.github.ignoramuses.bing_bing_wahoo.extensions.AbstractClientPlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.extensions.KeyboardInputExtensions;
import io.github.ignoramuses.bing_bing_wahoo.extensions.LocalPlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.extensions.PlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.JumpType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo.*;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer
		implements PlayerExtensions, LocalPlayerExtensions, AbstractClientPlayerExtensions {
	@Shadow public Input input;
	@Shadow private boolean lastOnGround;
	@Shadow private boolean handsBusy;
	@Shadow private boolean wasShiftKeyDown;

	public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Shadow public abstract boolean isShiftKeyDown();
	@Shadow public abstract float getViewXRot(float tickDelta);
	@Shadow public abstract float getViewYRot(float tickDelta);
	@Shadow public abstract boolean startRiding(Entity entity, boolean force);
	@Shadow protected abstract boolean hasEnoughImpulseToStartSprinting();

	@Unique private final MutableBlockPos lastPos = new MutableBlockPos();
	@Unique private boolean jumpHeldSinceLastJump = false;
	@Unique private boolean lastJumping = false;
	@Unique private long ticksSinceSneakingChanged = 0;
	@Unique private long ticksLeftToLongJump = 0;
	@Unique private long ticksLeftToDoubleJump = 0;
	@Unique private long ticksLeftToTripleJump = 0;
	@Unique private JumpType previousJumpType = JumpType.NORMAL;
	@Unique private boolean midTripleJump = false;
	@Unique private long tripleJumpTicks = 0;
	@Unique private boolean isDiving = false;
	@Unique private Vec3 currentDivingVelocity = Vec3.ZERO;
	@Unique private boolean bonked = false;
	@Unique private long bonkTime = 0;
	@Unique private boolean diveFlip = false;
	@Unique private int flipTimer = 0;
	@Unique private long ticksLeftToWallJump = 0;
	@Unique private boolean startingGroundPound = false;
	@Unique private long ticksInAirDuringGroundPound = 0;
	@Unique private double groundPoundSpeedMultiplier = 1.0;
	@Unique private boolean wallJumping = false;
	@Unique private boolean isGroundPounding = false;
	@Unique private boolean hasGroundPounded = false;
	@Unique private boolean ledgeGrabbing = false;
	@Unique private long ledgeGrabCooldown = 0;
	@Unique private long ledgeGrabExitCooldown = 0;
	@Unique private boolean longJumping = false;
	@Unique private boolean isBackFlipping = false;
	@Unique private long ticksGroundPounded = 0;
	@Unique private long diveCooldown = 0;
	@Unique private long bonkCooldown = 0;
	@Unique private boolean lastRiding = false;
	@Unique private boolean slidingOnGround = false;
	@Unique private boolean slidingOnSlope = false;
	@Unique private boolean wasRiding = false;
	@Unique private boolean canWahoo = false;
	@Unique private long ticksStillInDive = 0;
	@Unique private boolean forwardSliding = false;
	@Unique private long ticksSlidingOnGround = 0;

	@Inject(
			method = "onSyncedDataUpdated",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/resources/sounds/ElytraOnPlayerSoundInstance;<init>(Lnet/minecraft/client/player/LocalPlayer;)V"
			)
	)
	public void onFallFlyingStatusChange(CallbackInfo ci) {
		if (isDiving)
			exitDive();
		if (isGroundPounding)
			exitGroundPound();
		if (forwardSliding)
			exitForwardSlide();
	}

	@Inject(at = @At("HEAD"), method = "aiStep")
	public void savePreviousValues(CallbackInfo ci) {
		lastRiding = handsBusy;
		lastJumping = input.jumping;
		lastPos.set(blockPosition());
	}

	@Inject(at = @At("RETURN"), method = "aiStep")
	public void wahooLogic(CallbackInfo ci) {
		tickTimers();
		
		canWahoo = false;
		if (SyncedConfig.CAP_REQUIRED.get()) {
			if (getItemBySlot(EquipmentSlot.HEAD).is(WahooRegistry.MYSTERIOUS_CAP)
			|| TrinketsCompat.capTrinketEquipped(this)) {
				canWahoo = true;
			}
		} else {
			canWahoo = true;
		}

		if (jumpHeldSinceLastJump && lastJumping && !jumping) {
			jumpHeldSinceLastJump = false;
		}
		
		if (isPassenger()) {
			wasRiding = true;
		} else if (wasRiding) {
			if (onGround()) {
				wasRiding = false;
			}
		}

		Level level = level();

		// ----- TRIPLE JUMPS -----
		
		if (midTripleJump) {
			tripleJumpTicks++;
			if (tripleJumpTicks > 10) {
				StartFallFlyPacket.send();
			}
			if ((onGround() || !level.getFluidState(blockPosition()).isEmpty()) && tripleJumpTicks > 3 || isDiving || isGroundPounding || getAbilities().flying) {
				exitTripleJump();
			}
			
		}
		
		// ----- DIVING AND SLIDING -----
		
		if (isDiving || forwardSliding) {
			BlockPos floorPos = blockPosition();
			BlockState floor = level.getBlockState(floorPos);
			if (!WahooUtils.blockIsSlope(floor)) {
				floorPos = blockPosition().below();
				floor = level.getBlockState(floorPos);
			}
			if (onGround()) {
				if (floor.is(SLIDES)) {
					if (floor.getBlock() instanceof HorizontalDirectionalBlock || floor.getBlock() instanceof StairBlock) {
						handleSlidingOnSlope(floor);
					} else {
						// assume flat surface
						handleSlidingOnGround(true);
					}
				} else {
					// not on a slide, slow you down faster
					handleSlidingOnGround(false);
				}
			} else {
				if (lastOnGround) {
					if (floor.is(SLIDES) || level.getBlockState(floorPos.below()).is(SLIDES) || level.getBlockState(floorPos.below(2)).is(SLIDES)) {
						push(0, -5, 0);
					}
				} else {
					setDeltaMovement(currentDivingVelocity.x(), getDeltaMovement().y() - 0.05, currentDivingVelocity.z());
				}
			}
			
			if (WahooUtils.aprox(currentDivingVelocity.x(), 0, 0.07) &&
					WahooUtils.aprox(currentDivingVelocity.z(), 0, 0.07)) {
				ticksStillInDive++;
			} else {
				ticksStillInDive = 0;
			}
			
			if (ticksStillInDive > 20) {
				if (isDiving) exitDive();
				if (forwardSliding) exitForwardSlide();
			} else if (ticksSlidingOnGround > 50) {
				if (!floor.is(SLIDES)) {
					if (isDiving) exitDive();
					if (forwardSliding) exitForwardSlide();
				} else if (ticksSlidingOnGround > 100) {
					if (isDiving) exitDive();
					if (forwardSliding) exitForwardSlide();
				}
			}
			
			if (!level.getFluidState(blockPosition()).isEmpty() || getAbilities().flying) {
				if (isDiving) exitDive();
				if (forwardSliding) exitForwardSlide();
			}
			
			Direction looking = Direction.fromYRot(getYRot());
			Direction moving = WahooUtils.getHorizontalDirectionFromVector(getDeltaMovement());
			BlockPos posInFront = blockPosition().relative(looking);
			if (forwardSliding) posInFront = posInFront.below();
			BlockState inFront = level.getBlockState(posInFront);
			
			if (horizontalCollision && !inFront.isAir() && !WahooUtils.canGoUpSlope(inFront, moving)) {
				if (isDiving) exitDive();
				if (forwardSliding) exitForwardSlide();
				if (!ledgeGrabbing && !isCreative() && bonkCooldown == 0) {
					Vec3 dMovement = getDeltaMovement();
					boolean movingEnough = Math.abs(dMovement.x()) > 0.07 || Math.abs(dMovement.z()) > 0.07;
					if (movingEnough) {
						BlockPos offset = blockPosition().relative(looking);
						if (level.getBlockState(offset).isRedstoneConductor(level, offset) &&
								level.getBlockState(offset.above()).isRedstoneConductor(level, offset.above())) {
							bonk();
						}
					}
				}
			}
		}
		
		// Initiates Diving
		// ugly but it works
		if (diveCooldown == 0 && isSprinting() && !isDiving && !diveFlip && getPose() != Pose.SWIMMING && previousJumpType != JumpType.LONG && previousJumpType != JumpType.DIVE && !handsBusy && (onGround()
				? BingBingWahooConfig.groundedDives && Minecraft.getInstance().options.keyAttack.isDown()
				: Minecraft.getInstance().options.keyAttack.isDown())) {
			dive();
			previousJumpType = JumpType.DIVE;
		}
		
		// ----- BONKING -----
		
		// keep you bonked
		if (bonked) {
			if (ledgeGrabbing) {
				exitLedgeGrab(true);
			}
			if (getPose() != Pose.SLEEPING) {
				UpdatePosePacket.send(Pose.SLEEPING);
			}
			setDeltaMovement(getDeltaMovement().multiply(-0.8, 1, -0.8));
			--bonkTime;
			if (bonkTime == 0 || !level.getFluidState(blockPosition()).isEmpty()) {
				setDeltaMovement(0, getDeltaMovement().y(), 0);
				exitBonk();
			}
		}
		
		// ----- LEDGE GRABS -----
		
		Direction facing = getDirection();
		BlockPos head = blockPosition().above();
		BlockPos aboveHead = blockPosition().above(2);
		BlockPos inFrontOfHead = head.relative(facing);
		// checks block above and block adjacent to it in look direction
		if (!isDiving && !ledgeGrabbing && !isGroundPounding && !onGround() && !getAbilities().flying && !onClimbable() && !isSwimming() && ledgeGrabCooldown == 0 &&
				level.getBlockState(head).isAir() &&
				level.getBlockState(aboveHead).isAir() &&
				level.getBlockState(aboveHead.relative(facing)).isAir() &&
				!level.getBlockState(aboveHead.relative(facing)).is(Blocks.LADDER) &&
				// checks distance to block in front of eyes
				position().distanceTo(Vec3.atCenterOf(blockPosition().relative(facing))) < 1.2 &&
				WahooUtils.voxelShapeEligibleForGrab(level.getBlockState(inFrontOfHead).getCollisionShape(level, inFrontOfHead), facing)) {
			// slopes are kinda funky
			if (AutomobilityCompat.isSlope(level.getBlockState(inFrontOfHead))) {
				Direction blockFacing = level.getBlockState(inFrontOfHead).getValue(HorizontalDirectionalBlock.FACING);
				if (!blockFacing.getOpposite().equals(facing)) {
					ledgeGrab();
				}
			} else {
				ledgeGrab();
			}
		}
		
		if (ledgeGrabbing) {
			if (!level.getBlockState(inFrontOfHead.above()).isAir() ||
					!WahooUtils.voxelShapeEligibleForGrab(level.getBlockState(inFrontOfHead).getCollisionShape(level, inFrontOfHead), facing)) {
				exitLedgeGrab(true);
			}
			
			setDeltaMovement(0, 0, 0);
			if ((input.jumping || input.up) && ledgeGrabExitCooldown == 0) {
				exitLedgeGrab(false);
			} else if ((isShiftKeyDown() || input.down) && ledgeGrabExitCooldown == 0) {
				exitLedgeGrab(true);
			}
		}
		
		// ----- WALL JUMPS -----
		
		// runs on the first tick a player collides with a wall
		if (horizontalCollision) {
			if (ticksLeftToWallJump <= 0 && !isDiving && !onGround() && (
					BingBingWahooConfig.allowNormalWallJumps
							? previousJumpType.canWallJumpFrom() || previousJumpType == JumpType.NORMAL
							: previousJumpType.canWallJumpFrom())) {
				ticksLeftToWallJump = 4;
			}
		}
		
		// this is ugly but it works
		if (ticksLeftToWallJump > 0 && !isDiving && input.jumping && !jumpHeldSinceLastJump && (
				BingBingWahooConfig.allowNormalWallJumps
						? previousJumpType.canWallJumpFrom() || previousJumpType == JumpType.NORMAL
						: previousJumpType.canWallJumpFrom()) && !getAbilities().flying && !isInWater()) {
			wallJump();
		}
		
		if (wallJumping && onGround()) {
			exitWallJump();
		}
		
		// ----- GROUND POUND -----
		
		if (BingBingWahooConfig.groundPoundType.enabled() && !onGround() && isShiftKeyDown() && !wasShiftKeyDown && !longJumping && !getAbilities().flying && !handsBusy && !lastRiding && !isInWater() && !level.getBlockState(blockPosition().below()).isRedstoneConductor(level, blockPosition().below())) {
			List<Entity> entities = level.getEntities(this, new AABB(blockPosition()).inflate(1.5, 1, 1.5));
			boolean canPound = entities.size() == 0;
			if (canPound) {
				groundPound();
			}
		}
		
		if (isGroundPounding) {
			UpdatePosePacket.send(Pose.CROUCHING);
			hasGroundPounded = true;
			ticksInAirDuringGroundPound++;
			if (startingGroundPound) {
				setDeltaMovement(0, 0, 0);
			}
			
			if (startingGroundPound && flipTimer == 0) {
				startingGroundPound = false;
				UpdateFlipStatePacket.send(FlipState.NONE);
				if (BingBingWahooConfig.flipSpeedMultiplier != 0 && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
					setXRot(0);
				}
			}
			
			if (!startingGroundPound) {
				if (ticksInAirDuringGroundPound > 6) {
					setDeltaMovement(0, -0.5 * groundPoundSpeedMultiplier, 0);
					if (groundPoundSpeedMultiplier < 4) {
						groundPoundSpeedMultiplier = groundPoundSpeedMultiplier + 0.5;
					}
				}
			}
			
			if (hasGroundPounded && !startingGroundPound && lastPos.equals(blockPosition())) {
				ticksGroundPounded++;
				if (ticksGroundPounded > 5) {
					exitGroundPound();
				}
			}
			
			if (hasGroundPounded && !startingGroundPound && ticksGroundPounded > 0 && !lastPos.equals(blockPosition())) {
				ticksGroundPounded = 0;
			}
		}
		
		// ----- BACK FLIPS -----
		
		if (onGround() && isBackFlipping) {
			exitBackFlip();
		}
		
		// ----- LONG JUMPS -----
		
		if (longJumping && onGround()) {
			exitLongJump();
		}
		
		// ----- FORWARD SLIDING -----
		if ((WahooUtils.blockIsSlope(level.getBlockState(blockPosition())) ||
				WahooUtils.blockIsSlope(level.getBlockState(blockPosition().below()))) && isShiftKeyDown()) {
			startForwardSliding();
		}
	}

	@Unique
	private void handleSlidingOnSlope(BlockState floor) {
		ticksSlidingOnGround = 0;
		slidingOnSlope = true;
		slidingOnGround = false;
		Direction facing = floor.getValue(HorizontalDirectionalBlock.FACING);
		if (AutomobilityCompat.isSlope(floor)) {
			facing = facing.getOpposite();
		}
		double velocityToAdd = WahooUtils.getVelocityForSlopeDirection(facing);
		Vec3 newVelocity;
		if (facing.getAxis().equals(Direction.Axis.Z)) {
			// north and south
			newVelocity = new Vec3(
					currentDivingVelocity.x(),
					currentDivingVelocity.y() - 1,
					WahooUtils.capWithSign(currentDivingVelocity.z() + velocityToAdd, Math.max(0.7 - (ticksSlidingOnGround * 0.05), 0))
			);
		} else if (facing.getAxis().equals(Direction.Axis.X)) {
			// east and west
			newVelocity = new Vec3(
					WahooUtils.capWithSign(currentDivingVelocity.x() + velocityToAdd, Math.max(0.7 - (ticksSlidingOnGround * 0.05), 0)),
					currentDivingVelocity.y() - 1,
					currentDivingVelocity.z()
			);
		} else {
			// up and down? how did this happen?
			throw new RuntimeException("what");
		}
		currentDivingVelocity = newVelocity;
		setDeltaMovement(newVelocity);
	}
	
	@Unique
	private void handleSlidingOnGround(boolean slide) {
		ticksSlidingOnGround++;
		slidingOnSlope = false;
		slidingOnGround = true;
		Direction moving = WahooUtils.getHorizontalDirectionFromVector(currentDivingVelocity);
		Direction looking = WahooUtils.getHorizontalDirectionFromVector(getLookAngle());
		
		Vec3 newVelocity = currentDivingVelocity;
		
		if (looking != moving.getOpposite()) {
			double velocityToAdd = WahooUtils.getVelocityForSlidingOnGround(looking); // move towards look direction
			if (looking.getAxis().equals(Direction.Axis.Z)) {
				// north and south
				newVelocity = new Vec3(
						currentDivingVelocity.x() * 0.8,
						currentDivingVelocity.y(),
						WahooUtils.capWithSign(currentDivingVelocity.z() + velocityToAdd, Math.max(0.7 - (ticksSlidingOnGround * (slide ? 0.01 : 0.02)), 0))
				);
			} else if (looking.getAxis().equals(Direction.Axis.X)) {
				// east and west
				newVelocity = new Vec3(
						WahooUtils.capWithSign(currentDivingVelocity.x() + velocityToAdd, Math.max(0.7 - (ticksSlidingOnGround * (slide ? 0.01 : 0.02)), 0)),
						currentDivingVelocity.y(),
						currentDivingVelocity.z() * 0.8
				);
			} else {
				// up and down? how did this happen?
				throw new RuntimeException("what");
			}
		}
		
		Vec3 divingVelocity;
		if (slide) {
			if (ticksSlidingOnGround > 100) {
				divingVelocity = new Vec3(newVelocity.x() * 0.95, 0, newVelocity.z() * 0.95);
			} else {
				divingVelocity = newVelocity;
			}
		} else {
			if (ticksSlidingOnGround > 50) {
				divingVelocity = new Vec3(newVelocity.x() * 0.95, 0, newVelocity.z() * 0.95);
			} else {
				divingVelocity = newVelocity;
			}
		}
		
		setDeltaMovement(divingVelocity);
		currentDivingVelocity = divingVelocity;
	}
	
	/**
	 * Handles triggering of jump-based physics
	 */
	@Override
	public void jumpFromGround() {
		if (bonked) {
			return;
		}
		
		super.jumpFromGround();
		if (input.jumping) {
			if ((onGround())) {
				if (isDiving || forwardSliding) {
					diveFlip = true;
					flipTimer = 15;
					push(0, 0.25, 0);
					setDeltaMovement(getDeltaMovement().multiply(1.25, 1, 1.25));
					UpdateFlipStatePacket.send(FlipState.FORWARDS);
				} else if (((SyncedConfig.RAPID_FIRE_LONG_JUMPS.get()) || ticksLeftToLongJump > 0) && (isShiftKeyDown() || wasShiftKeyDown) && previousJumpType.canLongJumpFrom() && (isSprinting() || hasEnoughImpulseToStartSprinting() || input.down)) {
					longJump();
				} else if (ticksLeftToDoubleJump > 0 && !jumpHeldSinceLastJump && previousJumpType == JumpType.NORMAL) {
					doubleJump();
				} else if (ticksLeftToTripleJump > 0 && ticksLeftToTripleJump < 5 && previousJumpType == JumpType.DOUBLE && (isSprinting() || hasEnoughImpulseToStartSprinting() || input.down)) {
					tripleJump();
				} else if (isShiftKeyDown() && getDeltaMovement().x() == 0 && getDeltaMovement().z() == 0 && !jumpHeldSinceLastJump && BingBingWahooConfig.backFlips) {
					backFlip();
				} else {
					previousJumpType = JumpType.NORMAL;
				}
			}
		}
		lastJumping = true;
		jumpHeldSinceLastJump = true;
		UpdatePreviousJumpTypePacket.send(previousJumpType);
	}
	
	@Unique
	private void tickTimers() {
		// double jump
		if (!lastOnGround && onGround()) {
			ticksLeftToDoubleJump = 6;
		}
		if (ticksLeftToDoubleJump > 0) {
			ticksLeftToDoubleJump--;
		}
		
		// triple jump
		if (!lastOnGround && onGround()) {
			ticksLeftToTripleJump = 6;
		}
		if (ticksLeftToTripleJump > 0) {
			ticksLeftToTripleJump--;
		}
		
		// long jump
		if (isShiftKeyDown() != wasShiftKeyDown) {
			ticksSinceSneakingChanged = 0;
		}
		ticksSinceSneakingChanged++;
		if (ticksSinceSneakingChanged == 1 && isShiftKeyDown()) {
			ticksLeftToLongJump = 10;
		}
		if (ticksLeftToLongJump > 0) {
			ticksLeftToLongJump--;
		}
		
		// wall jump
		if (ticksLeftToWallJump > 0) {
			ticksLeftToWallJump--;
			
			if (ticksLeftToWallJump == 0 && previousJumpType != JumpType.NORMAL && !wallJumping && !ledgeGrabbing && !isCreative() && bonkCooldown == 0 && !(Math.abs(getDeltaMovement().x()) < 0.07 && Math.abs(getDeltaMovement().y()) < 0.07)) {
				Direction looking = Direction.fromYRot(getYRot());
				Level level = level();
				if (level.getBlockState(blockPosition().relative(looking)).isRedstoneConductor(level, blockPosition().relative(looking)) &&
						level.getBlockState(blockPosition().relative(looking).above()).isRedstoneConductor(level, blockPosition().relative(looking).above())) {
					bonk();
				}
			}
		}
		
		// ledge grab
		if (ledgeGrabCooldown > 0) {
			ledgeGrabCooldown--;
		}
		if (ledgeGrabExitCooldown > 0) {
			ledgeGrabExitCooldown--;
		}
		
		// dive and slide
		if (!isDiving && !diveFlip && diveCooldown > 0) {
			diveCooldown--;
		}
		
		// bonk
		if (!bonked && bonkCooldown > 0) {
			bonkCooldown--;
		}
		
		// all flips
		if (flipTimer > 0) {
			flipTimer--;
		}
	}

	@Override
	protected void updatePlayerPose() {
		if (isDiving || bonked || isGroundPounding) {
			return;
		}
		super.updatePlayerPose();
	}

	@Override
	public void setXRot(float pitch) {
		float tickDelta = Minecraft.getInstance().getFrameTime();
		if (isBackFlipping && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			if (BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setXRotRaw(-(Math.max(0, flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24) - 90);
			} else {
				super.setXRot(pitch);
			}
			return;
		}
		
		if (midTripleJump && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			if (BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setXRotRaw(Math.max(0, flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24);
			} else {
				super.setXRot(pitch);
			}
			if (flipTimer == 0) {
				exitTripleJump();
			}
			return;
		}
		
		if (diveFlip) {
			if (flipTimer == 0) {
				if (isDiving) exitDive();
				if (forwardSliding) exitForwardSlide();
			}
			
			if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setXRotRaw(Math.max(0, flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24);
			}
			return;
		}
		
		if (bonked) {
			return;
		}
		
		if (isGroundPounding && startingGroundPound) {
			if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setXRotRaw(Math.max(0, flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24);
			}
			return;
		}
		
		super.setXRot(pitch);
	}

	@Override
	public void setYRot(float yaw) {
		if (bonked || ledgeGrabbing) {
			return;
		}
		
		super.setYRot(yaw);
	}

	@Override
	public boolean getSliding() {
		return isDiving || forwardSliding;
	}

	@Override
	public boolean groundPounding() {
		return isGroundPounding;
	}

	@Override
	public boolean slidingOnSlope() {
		return slidingOnSlope && forwardSliding;
	}

	@Override
	public boolean slidingOnGround() {
		return slidingOnGround && forwardSliding;
	}
	
	// ---------- JUMPS ----------
	
	public void longJump() {
		if (!canWahoo || wasRiding) return;
		// ---------- warning: scary math ----------
		Vec2 velocity = new Vec2((float) getDeltaMovement().x(), (float) getDeltaMovement().z());
		Vec2 rotation = new Vec2((float) getLookAngle().x(), (float) getLookAngle().z());
		
		double cosOfVecs = (rotation.dot(velocity)) / (rotation.length() * velocity.length());
		double degreesDiff = Math.toDegrees(Math.acos(cosOfVecs));
		// ----------- end of scary math -----------
		
		// todo: use this to make a cleaner implementation
//		float x = MathHelper.sin(getYaw() * (float) (Math.PI / 180.0)) * MathHelper.cos(getPitch() * (float) (Math.PI / 180.0));
//		float z = MathHelper.cos(getYaw() * (float) (Math.PI / 180.0)) * MathHelper.cos(getPitch() * (float) (Math.PI / 180.0));
//		this.setVelocity(x * 0.5, 0.75, z * 0.5);
		
		if (degreesDiff > 85 && degreesDiff < 95) { // don't long jump for moving straight left or right
			ticksLeftToLongJump = 0;
			previousJumpType = JumpType.NORMAL;
			return;
		}
		
		double newVelXAbs;
		double newVelZAbs;
		
		double multiplier = SyncedConfig.LONG_JUMP_SPEED_MULTIPLIER.get();
		double maxSpeed = SyncedConfig.MAX_LONG_JUMP_SPEED.get();
		
		if (degreesDiff > 170) { // BLJ
			if (SyncedConfig.BACKWARDS_LONG_JUMPS.get()) {
				newVelXAbs = Math.abs(getDeltaMovement().x()) * multiplier;
				newVelZAbs = Math.abs(getDeltaMovement().z()) * multiplier;
			} else {
				ticksLeftToLongJump = 0;
				previousJumpType = JumpType.NORMAL;
				return;
			}
		} else {
			newVelXAbs = Math.min(Math.abs(getDeltaMovement().x()) * multiplier, maxSpeed);
			newVelZAbs = Math.min(Math.abs(getDeltaMovement().z()) * multiplier, maxSpeed);
		}
		
		double newVelX = Math.copySign(newVelXAbs, getDeltaMovement().x());
		double newVelZ = Math.copySign(newVelZAbs, getDeltaMovement().z());
		
		// todo: see https://github.com/n64decomp/sm64/blob/ecd3d152fb7c6f658d18543c0f4e8147b50d5dde/src/game/mario.c#L863
		
		setDeltaMovement(newVelX, Math.min(getDeltaMovement().y() * 1.5, 1), newVelZ);
		ticksLeftToLongJump = 0;
		longJumping = true;
		previousJumpType = JumpType.LONG;
	}
	
	private void exitLongJump() {
		longJumping = false;
	}
	
	private void doubleJump() {
		if (!canWahoo || wasRiding) return;
		setDeltaMovement(getDeltaMovement().multiply(1, 1.75, 1));
		ticksLeftToDoubleJump = 0;
		previousJumpType = JumpType.DOUBLE;
	}
	
	private void tripleJump() {
		if (!canWahoo || wasRiding) return;
		setDeltaMovement(getDeltaMovement().multiply(1, 2.5, 1));
		ticksLeftToTripleJump = 0;
		previousJumpType = JumpType.TRIPLE;
		midTripleJump = true;
		flipTimer = 40;
		UpdateFlipStatePacket.send(FlipState.FORWARDS);
	}
	
	public void exitTripleJump() {
		midTripleJump = false;
		tripleJumpTicks = 0;
		flipTimer = 0;
		UpdateFlipStatePacket.send(FlipState.NONE);
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			setXRot(0);
		}
	}
	
	private void dive() {
		if (!canWahoo || wasRiding || isBackFlipping || isFallFlying()) return;
		if (midTripleJump) exitTripleJump();
		if (wallJumping) exitWallJump();
		Level level = level();
		if (level.getBlockState(blockPosition()).isAir() && level.getBlockState(blockPosition().above()).isAir()) {
			setPosRaw(position().x(), position().y() + 1, position().z());
		}
		isDiving = true;
		UpdatePosePacket.send(Pose.SWIMMING);
		currentDivingVelocity = new Vec3(
				Math.copySign(Math.min(1.5, Math.abs(getDeltaMovement().x()) * 2.25), getDeltaMovement().x()),
				getDeltaMovement().y(),
				Math.copySign(Math.min(1.5, Math.abs(getDeltaMovement().z()) * 2.25), getDeltaMovement().z())
		);
		setDeltaMovement(currentDivingVelocity.add(0, 0.5, 0));
		diveCooldown = 20;
		UpdateDivePacket.sendStart(blockPosition());
	}
	
	public void exitDive() {
		slidingOnSlope = false;
		slidingOnGround = false;
		isDiving = false;
		ticksSlidingOnGround = 0;
		flipTimer = 0;
		UpdateFlipStatePacket.send(FlipState.NONE);
		ticksStillInDive = 0;
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && diveFlip && Minecraft.getInstance().options.getCameraType().isFirstPerson()) setXRot(0);
		diveFlip = false;
		UpdateDivePacket.sendStop();
	}
	
	public void bonk() {
		if (!canWahoo || wasRiding || !BingBingWahooConfig.bonking) return;
		((KeyboardInputExtensions) input).disableControl();
		if (isDiving) exitDive();
		if (midTripleJump) {
			exitTripleJump();
		}
		
		if (wallJumping) {
			exitWallJump();
		}
		
		setDeltaMovement(-getDeltaMovement().x(), getDeltaMovement().y(), -getDeltaMovement().z());
		UpdateBonkPacket.send(true);
		setXRot(-90);
		bonked = true;
		bonkTime = 30;
		bonkCooldown = 20;
	}
	
	public void exitBonk() {
		((KeyboardInputExtensions) input).enableControl();
		bonked = false;
		UpdateBonkPacket.send(false);
		tryCheckInsideBlocks();
		bonkTime = 0;
		setXRot(0);
	}
	
	private void wallJump() {
		if (!canWahoo || wasRiding) return;
		if (midTripleJump) exitTripleJump();
		
		wallJumping = true;
		ticksLeftToWallJump = 0;
		previousJumpType = JumpType.WALL;
		Direction directionOfNearestWall = Direction.UP;
		double distanceToNearestWall = 1;
		for (Direction direction : Plane.HORIZONTAL) {
			BlockState adjacentState = level().getBlockState(blockPosition().relative(direction));
			if (!adjacentState.isAir()) {
				double distance = position().distanceTo(Vec3.atCenterOf(blockPosition().relative(direction)));
				if (distance <= distanceToNearestWall) {
					directionOfNearestWall = direction;
					distanceToNearestWall = distance;
				}
			}
		}
		
		if (directionOfNearestWall == Direction.UP) {
			// this shouldn't happen but it does somehow, just cancel the wall jump :bigbrain:
			exitWallJump();
			return;
		}
		
		Vec3 directionToGo = Vec3.atLowerCornerOf(directionOfNearestWall.getOpposite().getNormal());
		this.setDeltaMovement(directionToGo.x() / 2, 0.75, directionToGo.z() / 2);
	}
	
	private void exitWallJump() {
		wallJumping = false;
	}
	
	private void ledgeGrab() {
		if (!canWahoo || wasRiding) return;
		if (midTripleJump) exitTripleJump();
		if (isDiving) exitDive();
		if (forwardSliding) exitForwardSlide();
		if (wallJumping) exitWallJump();
		
		setYRot(getDirection().toYRot());
		ledgeGrabbing = true;
		ledgeGrabExitCooldown = 10;
		bonkCooldown = 20;
	}
	
	private void exitLedgeGrab(boolean fall) {
		ledgeGrabbing = false;
		ledgeGrabCooldown = 20;
		if (!fall) {
			setDeltaMovement(0, 0.75, 0);
		}
	}
	
	private void groundPound() {
		if (!canWahoo || wasRiding || isGroundPounding) return;
		if (midTripleJump) exitTripleJump();
		if (wallJumping) exitWallJump();
		if (isDiving) exitDive();
		
		isGroundPounding = true;
		startingGroundPound = true;
		flipTimer = 15;
		UpdateFlipStatePacket.send(FlipState.FORWARDS);
		GroundPoundPacket.send(true);
	}
	
	private void exitGroundPound() {
		isGroundPounding = false;
		hasGroundPounded = false;
		ticksInAirDuringGroundPound = 0;
		flipTimer = 0;
		UpdateFlipStatePacket.send(FlipState.NONE);
		ticksGroundPounded = 0;
		GroundPoundPacket.send(false);
	}
	
	private void backFlip() {
		if (!canWahoo || wasRiding) return;
		isBackFlipping = true;
		flipTimer = 20;
		UpdateFlipStatePacket.send(FlipState.BACKWARDS);
		float x = -Mth.sin(getYRot() * (float) (Math.PI / 180.0)) * Mth.cos(getXRot() * (float) (Math.PI / 180.0));
		float z = Mth.cos(getYRot() * (float) (Math.PI / 180.0)) * Mth.cos(getXRot() * (float) (Math.PI / 180.0));
		this.setDeltaMovement(-x * 0.5, 1, -z * 0.5);
		previousJumpType = JumpType.BACK_FLIP;
	}
	
	private void exitBackFlip() {
		flipTimer = 0;
		UpdateFlipStatePacket.send(FlipState.NONE);
		isBackFlipping = false;
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && Minecraft.getInstance().options.getCameraType().isFirstPerson()) setXRot(0);
	}
	
	private void startForwardSliding() {
		if (!canWahoo || wasRiding) return;
		if (getDeltaMovement() != null) currentDivingVelocity = getDeltaMovement();
		forwardSliding = true;
		UpdateSlidePacket.send(true);
	}
	
	private void exitForwardSlide() {
		isDiving = false;
		slidingOnSlope = false;
		slidingOnGround = false;
		forwardSliding = false;
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && diveFlip && Minecraft.getInstance().options.getCameraType().isFirstPerson()) setXRot(0);
		diveFlip = false;
		flipTimer = 0;
		UpdateFlipStatePacket.send(FlipState.NONE);
		ticksSlidingOnGround = 0;
		ticksStillInDive = 0;
		UpdateSlidePacket.send(false);
	}

	@Override
	public int ticksFlipping() {
		return flipTimer;
	}

	@Override
	public void setFlipState(FlipState state) {
		flipTimer = state != FlipState.NONE ? 1 : 0;
	}

	@Override
	public boolean flippingForwards() {
		return ticksFlipping() > 0 && !isBackFlipping;
	}
}
