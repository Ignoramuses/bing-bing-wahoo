package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ignoramuses.bingBingWahoo.*;
import net.ignoramuses.bingBingWahoo.compat.AutomobilityCompat;
import net.ignoramuses.bingBingWahoo.compat.TrinketsCompat;
import net.ignoramuses.bingBingWahoo.extensions.AbstractClientPlayerExtensions;
import net.ignoramuses.bingBingWahoo.extensions.KeyboardInputExtensions;
import net.ignoramuses.bingBingWahoo.extensions.LocalPlayerExtensions;
import net.ignoramuses.bingBingWahoo.extensions.PlayerExtensions;
import net.ignoramuses.bingBingWahoo.movement.GroundPoundTypes;
import net.ignoramuses.bingBingWahoo.movement.JumpTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.*;
import static net.ignoramuses.bingBingWahoo.WahooCommands.*;
import static net.ignoramuses.bingBingWahoo.WahooNetworking.*;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerExtensions, LocalPlayerExtensions, AbstractClientPlayerExtensions {
	@Unique
	private final BlockPos.MutableBlockPos wahoo$lastPos = new BlockPos.MutableBlockPos();
	@Shadow
	public Input input;
	@Unique
	private boolean wahoo$jumpHeldSinceLastJump = false;
	@Unique
	private boolean wahoo$lastJumping = false;
	@Shadow
	private boolean lastOnGround;
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
	private Vec3 wahoo$currentDivingVelocity = Vec3.ZERO;
	@Unique
	private boolean wahoo$bonked = false;
	@Unique
	private long wahoo$bonkTime = 0;
	@Unique
	private boolean wahoo$diveFlip = false;
	@Unique
	private int wahoo$flipTimer = 0;
	@Unique
	private long wahoo$ticksLeftToWallJump = 0;
	@Unique
	private boolean wahoo$incipientGroundPound = false; // variables are not the place to show off your vocabulary
	@Unique
	private long wahoo$ticksInAirDuringGroundPound = 0;
	@Unique
	private double wahoo$groundPoundSpeedMultiplier = 1.0;
	@Unique
	private boolean wahoo$wallJumping = false;
	@Unique
	private boolean wahoo$isGroundPounding = false;
	@Unique
	private boolean wahoo$hasGroundPounded = false;
	@Unique
	private boolean wahoo$ledgeGrabbing = false;
	@Unique
	private long wahoo$ledgeGrabCooldown = 0;
	@Unique
	private long wahoo$ledgeGrabExitCooldown = 0;
	@Unique
	private boolean wahoo$longJumping = false;
	@Unique
	private boolean wahoo$isBackFlipping = false;
	@Unique
	private long wahoo$ticksGroundPounded = 0;
	@Unique
	private long wahoo$diveCooldown = 0;
	@Unique
	private long wahoo$bonkCooldown = 0;
	@Unique
	private boolean wahoo$lastRiding = false;
	@Unique
	private boolean wahoo$slidingOnGround = false;
	@Unique
	private boolean wahoo$slidingOnSlope = false;
	@Unique
	private boolean wahoo$wasRiding = false;
	@Unique
	private boolean wahoo$canWahoo = false;
	@Unique
	private long wahoo$ticksStillInDive = 0;
	@Unique
	private boolean wahoo$forwardSliding = false;
	@Unique
	private long wahoo$ticksSlidingOnGround = 0;
	@Unique
	private boolean wahoo$forwardsFlipping;

	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
		super(clientLevel, gameProfile, profilePublicKey);
	}

	@Shadow
	public abstract boolean isShiftKeyDown();
	
	@Shadow
	public abstract float getViewXRot(float tickDelta);

	@Shadow
	public abstract float getViewYRot(float tickDelta);
	
	@Shadow
	public abstract boolean startRiding(Entity entity, boolean force);
	
	@Shadow
	private boolean handsBusy;
	
	@Shadow
	private boolean wasShiftKeyDown;
	
	@Shadow
	protected abstract boolean hasEnoughImpulseToStartSprinting();

	@Inject(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/ElytraOnPlayerSoundInstance;<init>(Lnet/minecraft/client/player/LocalPlayer;)V"))
	public void wahoo$syncedDataUpdated(CallbackInfo ci) {
		if (wahoo$isDiving)
			exitDive();
		if (wahoo$isGroundPounding)
			exitGroundPound();
		if (wahoo$forwardSliding)
			exitForwardSlide();
	}

	@Inject(at = @At("HEAD"), method = "aiStep")
	public void wahoo$aiStepHEAD(CallbackInfo ci) {
		wahoo$lastRiding = handsBusy;
		wahoo$lastJumping = input.jumping;
		wahoo$lastPos.set(blockPosition());
	}
	
	/**
	 * Handles most tick-based physics, and when stuff should happen
	 */
	@Inject(at = @At("RETURN"), method = "aiStep")
	public void wahoo$aiStep(CallbackInfo ci) {
		updateJumpTicks();
		
		wahoo$canWahoo = false;
		if (BingBingWahooClient.getBooleanValue(HAT_REQUIRED_RULE)) {
			if (getItemBySlot(EquipmentSlot.HEAD).is(MYSTERIOUS_CAP)
			|| TrinketsCompat.capTrinketEquipped(this)) {
				wahoo$canWahoo = true;
			}
		} else {
			wahoo$canWahoo = true;
		}

		if (wahoo$jumpHeldSinceLastJump && wahoo$lastJumping && !jumping) {
			wahoo$jumpHeldSinceLastJump = false;
		}
		
		if (isPassenger()) {
			wahoo$wasRiding = true;
		} else if (wahoo$wasRiding) {
			if (isOnGround()) {
				wahoo$wasRiding = false;
			}
		}
		
		// ----- TRIPLE JUMPS -----
		
		if (wahoo$midTripleJump) {
			wahoo$tripleJumpTicks++;
			if (wahoo$tripleJumpTicks > 10) {
				ClientPlayNetworking.send(START_FALL_FLY, PacketByteBufs.empty());
			}
			if ((isOnGround() || !level.getFluidState(blockPosition()).isEmpty()) && wahoo$tripleJumpTicks > 3 || wahoo$isDiving || wahoo$isGroundPounding || getAbilities().flying) {
				exitTripleJump();
			}
			
		}
		
		// ----- DIVING AND SLIDING -----
		
		if (wahoo$isDiving || wahoo$forwardSliding) {
			BlockPos floorPos = blockPosition();
			BlockState floor = level.getBlockState(floorPos);
			if (!WahooUtils.blockIsSlope(floor)) {
				floorPos = blockPosition().below();
				floor = level.getBlockState(floorPos);
			}
			if (isOnGround()) {
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
					setDeltaMovement(wahoo$currentDivingVelocity.x(), getDeltaMovement().y() - 0.05, wahoo$currentDivingVelocity.z());
				}
			}
			
			if (WahooUtils.aprox(wahoo$currentDivingVelocity.x(), 0, 0.07) &&
					WahooUtils.aprox(wahoo$currentDivingVelocity.z(), 0, 0.07)) {
				wahoo$ticksStillInDive++;
			} else {
				wahoo$ticksStillInDive = 0;
			}
			
			if (wahoo$ticksStillInDive > 20) {
				if (wahoo$isDiving) exitDive();
				if (wahoo$forwardSliding) exitForwardSlide();
			} else if (wahoo$ticksSlidingOnGround > 50) {
				if (!floor.is(SLIDES)) {
					if (wahoo$isDiving) exitDive();
					if (wahoo$forwardSliding) exitForwardSlide();
				} else if (wahoo$ticksSlidingOnGround > 100) {
					if (wahoo$isDiving) exitDive();
					if (wahoo$forwardSliding) exitForwardSlide();
				}
			}
			
			if (!level.getFluidState(blockPosition()).isEmpty() || getAbilities().flying) {
				if (wahoo$isDiving) exitDive();
				if (wahoo$forwardSliding) exitForwardSlide();
			}
			
			Direction looking = Direction.fromYRot(getYRot());
			Direction moving = WahooUtils.getHorizontalDirectionFromVector(getDeltaMovement());
			BlockPos posInFront = blockPosition().relative(looking);
			if (wahoo$forwardSliding) posInFront = posInFront.below();
			BlockState inFront = level.getBlockState(posInFront);
			
			if (horizontalCollision && !inFront.isAir() && !WahooUtils.canGoUpSlope(inFront, moving)) {
				if (wahoo$isDiving) exitDive();
				if (wahoo$forwardSliding) exitForwardSlide();
				if (!wahoo$ledgeGrabbing && !isCreative() && wahoo$bonkCooldown == 0) {
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
		if (wahoo$diveCooldown == 0 && isSprinting() && !wahoo$isDiving && !wahoo$diveFlip && getPose() != Pose.SWIMMING && wahoo$previousJumpType != JumpTypes.LONG && wahoo$previousJumpType != JumpTypes.DIVE && !handsBusy && (isOnGround()
				? BingBingWahooConfig.groundedDives && Minecraft.getInstance().options.keyAttack.isDown()
				: Minecraft.getInstance().options.keyAttack.isDown())) {
			dive();
			wahoo$previousJumpType = JumpTypes.DIVE;
		}
		
		// ----- BONKING -----
		
		// keep you bonked
		if (wahoo$bonked) {
			if (wahoo$ledgeGrabbing) {
				exitLedgeGrab(true);
			}
			if (getPose() != Pose.SLEEPING) {
				ClientPlayNetworking.send(UPDATE_POSE, PacketByteBufs.create().writeVarInt(Pose.SLEEPING.ordinal()));
			}
			setDeltaMovement(getDeltaMovement().multiply(-0.8, 1, -0.8));
			--wahoo$bonkTime;
			if (wahoo$bonkTime == 0 || !level.getFluidState(blockPosition()).isEmpty()) {
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
		if (!wahoo$isDiving && !wahoo$ledgeGrabbing && !wahoo$isGroundPounding && !isOnGround() && !getAbilities().flying && !onClimbable() && !isSwimming() && wahoo$ledgeGrabCooldown == 0 &&
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
		
		if (wahoo$ledgeGrabbing) {
			if (!level.getBlockState(inFrontOfHead.above()).isAir() ||
					!WahooUtils.voxelShapeEligibleForGrab(level.getBlockState(inFrontOfHead).getCollisionShape(level, inFrontOfHead), facing)) {
				exitLedgeGrab(true);
			}
			
			setDeltaMovement(0, 0, 0);
			if ((input.jumping || input.up) && wahoo$ledgeGrabExitCooldown == 0) {
				exitLedgeGrab(false);
			} else if ((isShiftKeyDown() || input.down) && wahoo$ledgeGrabExitCooldown == 0) {
				exitLedgeGrab(true);
			}
		}
		
		// ----- WALL JUMPS -----
		
		// runs on the first tick a player collides with a wall
		if (horizontalCollision) {
			if (wahoo$ticksLeftToWallJump <= 0 && !wahoo$isDiving && !isOnGround() && (
					BingBingWahooConfig.allowNormalWallJumps
							? wahoo$previousJumpType.canWallJumpFrom() || wahoo$previousJumpType == JumpTypes.NORMAL
							: wahoo$previousJumpType.canWallJumpFrom())) {
				wahoo$ticksLeftToWallJump = 4;
			}
		}
		
		// this is ugly but it works
		if (wahoo$ticksLeftToWallJump > 0 && !wahoo$isDiving && input.jumping && !wahoo$jumpHeldSinceLastJump && (
				BingBingWahooConfig.allowNormalWallJumps
						? wahoo$previousJumpType.canWallJumpFrom() || wahoo$previousJumpType == JumpTypes.NORMAL
						: wahoo$previousJumpType.canWallJumpFrom()) && !getAbilities().flying && !isInWater()) {
			wallJump();
		}
		
		if (wahoo$wallJumping && isOnGround()) {
			exitWallJump();
		}
		
		// ----- GROUND POUND -----
		
		if (BingBingWahooConfig.groundPoundType.enabled() && !isOnGround() && isShiftKeyDown() && !wasShiftKeyDown && !wahoo$longJumping && !getAbilities().flying && !handsBusy && !wahoo$lastRiding && !isInWater() && !level.getBlockState(blockPosition().below()).isRedstoneConductor(level, blockPosition().below())) {
			List<Entity> entities = level.getEntities(this, new AABB(blockPosition()).inflate(1.5, 1, 1.5));
			boolean canPound = entities.size() == 0;
			if (canPound) {
				groundPound();
			}
		}
		
		if (wahoo$isGroundPounding) {
			ClientPlayNetworking.send(UPDATE_POSE, PacketByteBufs.create().writeVarInt(Pose.CROUCHING.ordinal()));
			wahoo$hasGroundPounded = true;
			wahoo$ticksInAirDuringGroundPound++;
			if (wahoo$incipientGroundPound) {
				setDeltaMovement(0, 0, 0);
			}
			
			if (wahoo$incipientGroundPound && wahoo$flipTimer == 0) {
				wahoo$incipientGroundPound = false;
				FriendlyByteBuf buf = PacketByteBufs.create();
				buf.writeBoolean(false);
				ClientPlayNetworking.send(UPDATE_FLIP, buf);
				if (BingBingWahooConfig.flipSpeedMultiplier != 0 && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
					setXRot(0);
				}
			}
			
			if (!wahoo$incipientGroundPound) {
				if (wahoo$ticksInAirDuringGroundPound > 6) {
					setDeltaMovement(0, -0.5 * wahoo$groundPoundSpeedMultiplier, 0);
					if (wahoo$groundPoundSpeedMultiplier < 4) {
						wahoo$groundPoundSpeedMultiplier = wahoo$groundPoundSpeedMultiplier + 0.5;
					}
				}
			}
			
			if (wahoo$hasGroundPounded && !wahoo$incipientGroundPound && wahoo$lastPos.equals(blockPosition())) {
				wahoo$ticksGroundPounded++;
				if (wahoo$ticksGroundPounded > 5) {
					exitGroundPound();
				}
			}
			
			if (wahoo$hasGroundPounded && !wahoo$incipientGroundPound && wahoo$ticksGroundPounded > 0 && !wahoo$lastPos.equals(blockPosition())) {
				wahoo$ticksGroundPounded = 0;
			}
		}
		
		// ----- BACK FLIPS -----
		
		if (isOnGround() && wahoo$isBackFlipping) {
			exitBackFlip();
		}
		
		// ----- LONG JUMPS -----
		
		if (wahoo$longJumping && isOnGround()) {
			exitLongJump();
		}
		
		// ----- FORWARD SLIDING -----
		if ((WahooUtils.blockIsSlope(level.getBlockState(blockPosition())) ||
				WahooUtils.blockIsSlope(level.getBlockState(blockPosition().below()))) && isShiftKeyDown()) {
			startForwardSliding();
		}
	}

	private void handleSlidingOnSlope(BlockState floor) {
		wahoo$ticksSlidingOnGround = 0;
		wahoo$slidingOnSlope = true;
		wahoo$slidingOnGround = false;
		Direction facing = floor.getValue(HorizontalDirectionalBlock.FACING);
		if (AutomobilityCompat.isSlope(floor)) {
			facing = facing.getOpposite();
		}
		double velocityToAdd = WahooUtils.getVelocityForSlopeDirection(facing);
		Vec3 newVelocity;
		if (facing.getAxis().equals(Direction.Axis.Z)) {
			// north and south
			newVelocity = new Vec3(
					wahoo$currentDivingVelocity.x(),
					wahoo$currentDivingVelocity.y() - 1,
					WahooUtils.capWithSign(wahoo$currentDivingVelocity.z() + velocityToAdd, Math.max(0.7 - (wahoo$ticksSlidingOnGround * 0.05), 0))
			);
		} else if (facing.getAxis().equals(Direction.Axis.X)) {
			// east and west
			newVelocity = new Vec3(
					WahooUtils.capWithSign(wahoo$currentDivingVelocity.x() + velocityToAdd, Math.max(0.7 - (wahoo$ticksSlidingOnGround * 0.05), 0)),
					wahoo$currentDivingVelocity.y() - 1,
					wahoo$currentDivingVelocity.z()
			);
		} else {
			// up and down? how did this happen?
			throw new RuntimeException("what");
		}
		wahoo$currentDivingVelocity = newVelocity;
		setDeltaMovement(newVelocity);
	}
	
	/**
	 * @param slide Whether the floor is in the slide tag or not
	 */
	private void handleSlidingOnGround(boolean slide) {
		wahoo$ticksSlidingOnGround++;
		wahoo$slidingOnSlope = false;
		wahoo$slidingOnGround = true;
		Direction moving = WahooUtils.getHorizontalDirectionFromVector(wahoo$currentDivingVelocity);
		Direction looking = WahooUtils.getHorizontalDirectionFromVector(getLookAngle());
		
		Vec3 newVelocity = wahoo$currentDivingVelocity;
		
		if (looking != moving.getOpposite()) {
			double velocityToAdd = WahooUtils.getVelocityForSlidingOnGround(looking); // move towards look direction
			if (looking.getAxis().equals(Direction.Axis.Z)) {
				// north and south
				newVelocity = new Vec3(
						wahoo$currentDivingVelocity.x() * 0.8,
						wahoo$currentDivingVelocity.y(),
						WahooUtils.capWithSign(wahoo$currentDivingVelocity.z() + velocityToAdd, Math.max(0.7 - (wahoo$ticksSlidingOnGround * (slide ? 0.01 : 0.02)), 0))
				);
			} else if (looking.getAxis().equals(Direction.Axis.X)) {
				// east and west
				newVelocity = new Vec3(
						WahooUtils.capWithSign(wahoo$currentDivingVelocity.x() + velocityToAdd, Math.max(0.7 - (wahoo$ticksSlidingOnGround * (slide ? 0.01 : 0.02)), 0)),
						wahoo$currentDivingVelocity.y(),
						wahoo$currentDivingVelocity.z() * 0.8
				);
			} else {
				// up and down? how did this happen?
				throw new RuntimeException("what");
			}
		}
		
		Vec3 divingVelocity;
		if (slide) {
			if (wahoo$ticksSlidingOnGround > 100) {
				divingVelocity = new Vec3(newVelocity.x() * 0.95, 0, newVelocity.z() * 0.95);
			} else {
				divingVelocity = newVelocity;
			}
		} else {
			if (wahoo$ticksSlidingOnGround > 50) {
				divingVelocity = new Vec3(newVelocity.x() * 0.95, 0, newVelocity.z() * 0.95);
			} else {
				divingVelocity = newVelocity;
			}
		}
		
		setDeltaMovement(divingVelocity);
		wahoo$currentDivingVelocity = divingVelocity;
	}
	
	/**
	 * Handles triggering of jump-based physics
	 */
	@Override
	public void jumpFromGround() {
		if (wahoo$bonked) {
			return;
		}
		
		super.jumpFromGround();
		if (input.jumping) {
			if ((isOnGround())) {
				if (wahoo$isDiving || wahoo$forwardSliding) {
					wahoo$diveFlip = true;
					wahoo$flipTimer = 15;
					push(0, 0.25, 0);
					setDeltaMovement(getDeltaMovement().multiply(1.25, 1, 1.25));
					FriendlyByteBuf buf = PacketByteBufs.create();
					buf.writeBoolean(true).writeBoolean(true);
					ClientPlayNetworking.send(UPDATE_FLIP, buf);
				} else if (((BingBingWahooConfig.rapidFireLongJumps && BingBingWahooClient.getBooleanValue(RAPID_FIRE_LONG_JUMPS_RULE)) || wahoo$ticksLeftToLongJump > 0) && (isShiftKeyDown() || wasShiftKeyDown) && wahoo$previousJumpType.canLongJumpFrom() && (isSprinting() || hasEnoughImpulseToStartSprinting() || input.down)) {
					longJump();
				} else if (wahoo$ticksLeftToDoubleJump > 0 && !wahoo$jumpHeldSinceLastJump && wahoo$previousJumpType == JumpTypes.NORMAL) {
					doubleJump();
				} else if (wahoo$ticksLeftToTripleJump > 0 && wahoo$ticksLeftToTripleJump < 5 && wahoo$previousJumpType == JumpTypes.DOUBLE && (isSprinting() || hasEnoughImpulseToStartSprinting() || input.down)) {
					tripleJump();
				} else if (isShiftKeyDown() && getDeltaMovement().x() == 0 && getDeltaMovement().z() == 0 && !wahoo$jumpHeldSinceLastJump && BingBingWahooConfig.backFlips) {
					backFlip();
				} else {
					wahoo$previousJumpType = JumpTypes.NORMAL;
				}
			}
		}
		wahoo$lastJumping = true;
		wahoo$jumpHeldSinceLastJump = true;
		ClientPlayNetworking.send(JUMP_TYPE_PACKET, wahoo$previousJumpType.toBuf());
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
		if (isShiftKeyDown() != wasShiftKeyDown) {
			wahoo$ticksSinceSneakingChanged = 0;
		}
		wahoo$ticksSinceSneakingChanged++;
		if (wahoo$ticksSinceSneakingChanged == 1 && isShiftKeyDown()) {
			wahoo$ticksLeftToLongJump = 10;
		}
		if (wahoo$ticksLeftToLongJump > 0) {
			wahoo$ticksLeftToLongJump--;
		}
		
		// wall jump
		if (wahoo$ticksLeftToWallJump > 0) {
			wahoo$ticksLeftToWallJump--;
			
			if (wahoo$ticksLeftToWallJump == 0 && wahoo$previousJumpType != JumpTypes.NORMAL && !wahoo$wallJumping && !wahoo$ledgeGrabbing && !isCreative() && wahoo$bonkCooldown == 0 && !(Math.abs(getDeltaMovement().x()) < 0.07 && Math.abs(getDeltaMovement().y()) < 0.07)) {
				Direction looking = Direction.fromYRot(getYRot());
				if (level.getBlockState(blockPosition().relative(looking)).isRedstoneConductor(level, blockPosition().relative(looking)) &&
						level.getBlockState(blockPosition().relative(looking).above()).isRedstoneConductor(level, blockPosition().relative(looking).above())) {
					bonk();
				}
			}
		}
		
		// ledge grab
		if (wahoo$ledgeGrabCooldown > 0) {
			wahoo$ledgeGrabCooldown--;
		}
		if (wahoo$ledgeGrabExitCooldown > 0) {
			wahoo$ledgeGrabExitCooldown--;
		}
		
		// dive and slide
		if (!wahoo$isDiving && !wahoo$diveFlip && wahoo$diveCooldown > 0) {
			wahoo$diveCooldown--;
		}
		
		// bonk
		if (!wahoo$bonked && wahoo$bonkCooldown > 0) {
			wahoo$bonkCooldown--;
		}
		
		// all flips
		if (wahoo$flipTimer > 0) {
			wahoo$flipTimer--;
		}
	}
	
	/**
	 * An override of updatePose to allow for custom handling, sleeping for bonking and swimming for diving, etc.
	 */
	@Override
	protected void updatePlayerPose() {
		if (wahoo$isDiving || wahoo$bonked || wahoo$isGroundPounding) {
			return;
		}
		super.updatePlayerPose();
	}

	/**
	 * Similar to {@link LocalPlayerMixin#updatePlayerPose}, allows for special handling of pitch changes
	 */
	@Override
	public void setXRot(float pitch) {
		float tickDelta = Minecraft.getInstance().getFrameTime();
		if (wahoo$isBackFlipping && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			if (BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).wahoo$setXRotRaw(-(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24) - 90);
			} else {
				super.setXRot(pitch);
			}
			return;
		}
		
		if (wahoo$midTripleJump && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			if (BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).wahoo$setXRotRaw(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24);
			} else {
				super.setXRot(pitch);
			}
			if (wahoo$flipTimer == 0) {
				exitTripleJump();
			}
			return;
		}
		
		if (wahoo$diveFlip) {
			if (wahoo$flipTimer == 0) {
				if (wahoo$isDiving) exitDive();
				if (wahoo$forwardSliding) exitForwardSlide();
			}
			
			if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).wahoo$setXRotRaw(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24);
			}
			return;
		}
		
		if (wahoo$bonked) {
			return;
		}
		
		if (wahoo$isGroundPounding && wahoo$incipientGroundPound) {
			if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && BingBingWahooConfig.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).wahoo$setXRotRaw(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooConfig.flipSpeedMultiplier * -24);
			}
			return;
		}
		
		super.setXRot(pitch);
	}
	
	/**
	 * Similar to {@link LocalPlayerMixin#updatePlayerPose}, allows for special handling of yaw changes
	 */
	@Override
	public void setYRot(float yaw) {
		if (wahoo$bonked || wahoo$ledgeGrabbing) {
			return;
		}
		
		super.setYRot(yaw);
	}

	@Override
	public boolean wahoo$getSliding() {
		return wahoo$isDiving || wahoo$forwardSliding;
	}

	@Override
	public boolean wahoo$groundPounding() {
		return wahoo$isGroundPounding;
	}

	@Override
	public boolean wahoo$slidingOnSlope() {
		return wahoo$slidingOnSlope && wahoo$forwardSliding;
	}

	@Override
	public boolean wahoo$slidingOnGround() {
		return wahoo$slidingOnGround && wahoo$forwardSliding;
	}
	
	// ---------- JUMPS ----------
	
	public void longJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
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
			wahoo$ticksLeftToLongJump = 0;
			wahoo$previousJumpType = JumpTypes.NORMAL;
			return;
		}
		
		double newVelXAbs;
		double newVelZAbs;
		
		double multiplier = Math.min(BingBingWahooConfig.longJumpSpeedMultiplier, BingBingWahooClient.getDoubleValue(LONG_JUMP_SPEED_MULTIPLIER_RULE));
		double maxSpeed = Math.min(BingBingWahooConfig.maxLongJumpSpeed, BingBingWahooClient.getDoubleValue(MAX_LONG_JUMP_SPEED_RULE));
		
		if (degreesDiff > 170) { // BLJ
			if (BingBingWahooClient.getBooleanValue(BACKWARDS_LONG_JUMPS_RULE) && BingBingWahooConfig.blj) {
				newVelXAbs = Math.abs(getDeltaMovement().x()) * multiplier;
				newVelZAbs = Math.abs(getDeltaMovement().z()) * multiplier;
			} else {
				wahoo$ticksLeftToLongJump = 0;
				wahoo$previousJumpType = JumpTypes.NORMAL;
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
		wahoo$ticksLeftToLongJump = 0;
		wahoo$longJumping = true;
		wahoo$previousJumpType = JumpTypes.LONG;
	}
	
	private void exitLongJump() {
		wahoo$longJumping = false;
	}
	
	private void doubleJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		setDeltaMovement(getDeltaMovement().multiply(1, 1.75, 1));
		wahoo$ticksLeftToDoubleJump = 0;
		wahoo$previousJumpType = JumpTypes.DOUBLE;
	}
	
	private void tripleJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		setDeltaMovement(getDeltaMovement().multiply(1, 2.5, 1));
		wahoo$ticksLeftToTripleJump = 0;
		wahoo$previousJumpType = JumpTypes.TRIPLE;
		wahoo$midTripleJump = true;
		wahoo$flipTimer = 40;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(true).writeBoolean(true);
		ClientPlayNetworking.send(UPDATE_FLIP, buf);
	}
	
	public void exitTripleJump() {
		wahoo$midTripleJump = false;
		wahoo$tripleJumpTicks = 0;
		wahoo$flipTimer = 0;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(false);
		ClientPlayNetworking.send(UPDATE_FLIP, buf);
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			setXRot(0);
		}
	}
	
	private void dive() {
		if (!wahoo$canWahoo || wahoo$wasRiding || wahoo$isBackFlipping || isFallFlying()) return;
		if (wahoo$midTripleJump) exitTripleJump();
		if (wahoo$wallJumping) exitWallJump();
		if (level.getBlockState(blockPosition()).isAir() && level.getBlockState(blockPosition().above()).isAir()) {
			setPosRaw(position().x(), position().y() + 1, position().z());
		}
		wahoo$isDiving = true;
		ClientPlayNetworking.send(UPDATE_POSE, PacketByteBufs.create().writeVarInt(Pose.SWIMMING.ordinal()));
		wahoo$currentDivingVelocity = new Vec3(
				Math.copySign(Math.min(1.5, Math.abs(getDeltaMovement().x()) * 2.25), getDeltaMovement().x()),
				getDeltaMovement().y(),
				Math.copySign(Math.min(1.5, Math.abs(getDeltaMovement().z()) * 2.25), getDeltaMovement().z())
		);
		setDeltaMovement(wahoo$currentDivingVelocity.add(0, 0.5, 0));
		wahoo$diveCooldown = 20;
		ClientPlayNetworking.send(DIVE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeBoolean(true)).writeBlockPos(blockPosition()));
	}
	
	public void exitDive() {
		wahoo$slidingOnSlope = false;
		wahoo$slidingOnGround = false;
		wahoo$isDiving = false;
		wahoo$ticksSlidingOnGround = 0;
		wahoo$flipTimer = 0;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(false);
		ClientPlayNetworking.send(UPDATE_FLIP, buf);
		wahoo$ticksStillInDive = 0;
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && wahoo$diveFlip && Minecraft.getInstance().options.getCameraType().isFirstPerson()) setXRot(0);
		wahoo$diveFlip = false;
		ClientPlayNetworking.send(DIVE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeBoolean(false)));
	}
	
	public void bonk() {
		if (!wahoo$canWahoo || wahoo$wasRiding || !BingBingWahooConfig.bonking) return;
		((KeyboardInputExtensions) input).wahoo$disableControl();
		if (wahoo$isDiving) exitDive();
		if (wahoo$midTripleJump) {
			exitTripleJump();
		}
		
		if (wahoo$wallJumping) {
			exitWallJump();
		}
		
		setDeltaMovement(-getDeltaMovement().x(), getDeltaMovement().y(), -getDeltaMovement().z());
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(true);
		ClientPlayNetworking.send(BONK_PACKET, buf);
		setXRot(-90);
		wahoo$bonked = true;
		wahoo$bonkTime = 30;
		wahoo$bonkCooldown = 20;
	}
	
	public void exitBonk() {
		((KeyboardInputExtensions) input).wahoo$enableControl();
		wahoo$bonked = false;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(false);
		ClientPlayNetworking.send(BONK_PACKET, buf);
		tryCheckInsideBlocks();
		wahoo$bonkTime = 0;
		setXRot(0);
	}
	
	private void wallJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		if (wahoo$midTripleJump) exitTripleJump();
		
		wahoo$wallJumping = true;
		wahoo$ticksLeftToWallJump = 0;
		wahoo$previousJumpType = JumpTypes.WALL;
		Direction directionOfNearestWall = Direction.UP;
		double distanceToNearestWall = 1;
		for (Direction direction : Plane.HORIZONTAL) {
			BlockState adjacentState = level.getBlockState(blockPosition().relative(direction));
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
		wahoo$wallJumping = false;
	}
	
	private void ledgeGrab() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		if (wahoo$midTripleJump) exitTripleJump();
		if (wahoo$isDiving) exitDive();
		if (wahoo$forwardSliding) exitForwardSlide();
		if (wahoo$wallJumping) exitWallJump();
		
		setYRot(getDirection().toYRot());
		wahoo$ledgeGrabbing = true;
		wahoo$ledgeGrabExitCooldown = 10;
		wahoo$bonkCooldown = 20;
	}
	
	private void exitLedgeGrab(boolean fall) {
		wahoo$ledgeGrabbing = false;
		wahoo$ledgeGrabCooldown = 20;
		if (!fall) {
			setDeltaMovement(0, 0.75, 0);
		}
	}
	
	private void groundPound() {
		if (!wahoo$canWahoo || wahoo$wasRiding || wahoo$isGroundPounding) return;
		if (wahoo$midTripleJump) exitTripleJump();
		if (wahoo$wallJumping) exitWallJump();
		if (wahoo$isDiving) exitDive();
		
		wahoo$isGroundPounding = true;
		wahoo$incipientGroundPound = true;
		wahoo$flipTimer = 15;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(true).writeBoolean(true);
		ClientPlayNetworking.send(UPDATE_FLIP, buf);
		ClientPlayNetworking.send(GROUND_POUND_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeBoolean(true)
				.writeBoolean(BingBingWahooConfig.groundPoundType == GroundPoundTypes.DESTRUCTIVE)));
	}
	
	private void exitGroundPound() {
		wahoo$isGroundPounding = false;
		wahoo$hasGroundPounded = false;
		wahoo$ticksInAirDuringGroundPound = 0;
		wahoo$flipTimer = 0;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(false);
		ClientPlayNetworking.send(UPDATE_FLIP, buf);
		wahoo$ticksGroundPounded = 0;
		ClientPlayNetworking.send(GROUND_POUND_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeBoolean(false)
				.writeBoolean(BingBingWahooConfig.groundPoundType == GroundPoundTypes.DESTRUCTIVE)));
	}
	
	private void backFlip() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		wahoo$isBackFlipping = true;
		wahoo$flipTimer = 20;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(true).writeBoolean(false);
		ClientPlayNetworking.send(UPDATE_FLIP, buf);
		float x = -Mth.sin(getYRot() * (float) (Math.PI / 180.0)) * Mth.cos(getXRot() * (float) (Math.PI / 180.0));
		float z = Mth.cos(getYRot() * (float) (Math.PI / 180.0)) * Mth.cos(getXRot() * (float) (Math.PI / 180.0));
		this.setDeltaMovement(-x * 0.5, 1, -z * 0.5);
		wahoo$previousJumpType = JumpTypes.BACK_FLIP;
	}
	
	private void exitBackFlip() {
		wahoo$flipTimer = 0;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(false);
		ClientPlayNetworking.send(UPDATE_FLIP, buf);
		wahoo$isBackFlipping = false;
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && Minecraft.getInstance().options.getCameraType().isFirstPerson()) setXRot(0);
	}
	
	private void startForwardSliding() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		if (getDeltaMovement() != null) wahoo$currentDivingVelocity = getDeltaMovement();
		wahoo$forwardSliding = true;
		ClientPlayNetworking.send(SLIDE_PACKET, new FriendlyByteBuf(PacketByteBufs.create().writeBoolean(true)));
	}
	
	private void exitForwardSlide() {
		wahoo$isDiving = false;
		wahoo$slidingOnSlope = false;
		wahoo$slidingOnGround = false;
		wahoo$forwardSliding = false;
		if (BingBingWahooConfig.flipSpeedMultiplier != 0 && wahoo$diveFlip && Minecraft.getInstance().options.getCameraType().isFirstPerson()) setXRot(0);
		wahoo$diveFlip = false;
		wahoo$flipTimer = 0;
		FriendlyByteBuf flipBuf = PacketByteBufs.create();
		flipBuf.writeBoolean(false);
		ClientPlayNetworking.send(UPDATE_FLIP, flipBuf);
		wahoo$ticksSlidingOnGround = 0;
		wahoo$ticksStillInDive = 0;
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(false);
		ClientPlayNetworking.send(SLIDE_PACKET, buf);
	}

	@Override
	public int wahoo$ticksFlipping() {
		return wahoo$flipTimer;
	}

	@Override
	public void wahoo$setFlipping(boolean value) {
		wahoo$flipTimer = value ? 1 : 0;
	}

	@Override
	public void wahoo$setFlipDirection(boolean forwards) {
	}

	@Override
	public boolean wahoo$flippingForwards() {
		return wahoo$ticksFlipping() > 0 && !wahoo$isBackFlipping;
	}
}
