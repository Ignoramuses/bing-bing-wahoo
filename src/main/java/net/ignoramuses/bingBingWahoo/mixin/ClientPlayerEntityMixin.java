package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ignoramuses.bingBingWahoo.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.*;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	@Unique
	private final BlockPos.Mutable wahoo$lastPos = new BlockPos.Mutable();
	@Shadow
	public Input input;
	@Unique
	private boolean wahoo$jumpHeldSinceLastJump = false;
	@Unique
	private boolean wahoo$lastJumping = false;
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
	private boolean wahoo$wasRiding = false;
	@Unique
	private boolean wahoo$canWahoo = false;
	@Shadow
	private boolean riding;
	
	private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}
	
	@Shadow
	public abstract boolean isSneaking();
	
	@Shadow
	public abstract float getPitch(float tickDelta);
	
	@Shadow
	protected abstract boolean isWalking();
	
	@Shadow
	public abstract float getYaw(float tickDelta);
	
	@Inject(at = @At("HEAD"), method = "tickMovement()V")
	public void wahoo$tickMovementHEAD(CallbackInfo ci) {
		wahoo$lastRiding = riding;
		wahoo$lastJumping = input.jumping;
		wahoo$lastPos.set(getBlockPos());
	}
	
	/**
	 * Handles most tick-based physics, and when stuff should happen
	 */
	@Inject(at = @At("RETURN"), method = "tickMovement()V")
	public void wahoo$tickMovement(CallbackInfo ci) {
		updateJumpTicks();
		
		wahoo$canWahoo = false;
		if ((boolean) BingBingWahooClient.GAME_RULES.get(HAT_REQUIRED_RULE.getName())) {
			if (getEquippedStack(EquipmentSlot.HEAD).isOf(MYSTERIOUS_CAP)) {
				wahoo$canWahoo = true;
			} else if (TRINKETS_LOADED) {
				if (TrinketsHandler.capEquipped(this)) {
					wahoo$canWahoo = true;
				}
			}
		} else {
			wahoo$canWahoo = true;
		}
		
		// I think this can be simplified but I'm too scared it will catastrophically fail if I try to
		if (wahoo$jumpHeldSinceLastJump) {
			if (wahoo$lastJumping) {
				if (!jumping) {
					wahoo$jumpHeldSinceLastJump = false;
				}
			}
		}
		
		if (hasVehicle()) {
			wahoo$wasRiding = true;
		} else if (wahoo$wasRiding) {
			if (isOnGround()) {
				wahoo$wasRiding = false;
			}
		}
		
		// ----- TRIPLE JUMPS -----
		
		if (wahoo$midTripleJump) {
			wahoo$tripleJumpTicks++;
			// number is actually irrelevant, is handled in our override
			if ((isOnGround() || !world.getFluidState(getBlockPos()).isEmpty()) && wahoo$tripleJumpTicks > 3 || wahoo$isDiving || wahoo$isGroundPounding || getAbilities().flying) {
				exitTripleJump();
			}
			
		}
		
		// ----- DIVING -----
		
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
				exitDive();
				if (!wahoo$ledgeGrabbing && !isCreative() && wahoo$bonkCooldown == 0 && !(Math.abs(getVelocity().getX()) < 0.07 && Math.abs(getVelocity().getY()) < 0.07)) {
					Direction looking = Direction.fromRotation(getYaw());
					if (world.getBlockState(getBlockPos().offset(looking)).isSolidBlock(world, getBlockPos().offset(looking)) &&
							world.getBlockState(getBlockPos().offset(looking).up()).isSolidBlock(world, getBlockPos().offset(looking).up())) {
						bonk();
					}
				}
			}
		}
		
		// Initiates Diving
		// ugly but it works
		if (wahoo$diveCooldown == 0 && isSprinting() && !wahoo$isDiving && !wahoo$diveFlip && getPose() != EntityPose.SWIMMING && wahoo$previousJumpType != JumpTypes.LONG && wahoo$previousJumpType != JumpTypes.DIVE && !riding && (isOnGround()
				? BingBingWahooClient.CONFIG.groundedDives && MinecraftClient.getInstance().options.keyAttack.isPressed()
				: MinecraftClient.getInstance().options.keyAttack.isPressed())) {
			dive();
			wahoo$previousJumpType = JumpTypes.DIVE;
		}
		
		// ----- BONKING -----
		
		// keep you bonked
		if (wahoo$bonked) {
			if (wahoo$ledgeGrabbing) {
				exitLedgeGrab(true);
			}
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
		
		// ----- LEDGE GRABS -----
		
		// checks block above and block adjacent to it in look direction
		if (world.getBlockState(getBlockPos().up().up()).isAir() &&
				world.getBlockState(getBlockPos().up().up().offset(getHorizontalFacing())).isAir() &&
				!world.getBlockState(getBlockPos().up().up().offset(getHorizontalFacing())).getBlock().equals(Blocks.LADDER) &&
				voxelShapeEligibleForGrab(world.getBlockState(getBlockPos().up().offset(getHorizontalFacing())).getCollisionShape(world, getBlockPos().up().offset(getHorizontalFacing()))) &&
				// checks distance to block in front of eyes
				getPos().distanceTo(Vec3d.ofCenter(getBlockPos().offset(getHorizontalFacing()))) < 1.2 &&
				!wahoo$ledgeGrabbing && !wahoo$isGroundPounding && !isOnGround() && !getAbilities().flying && !isClimbing() && !isSwimming() &&
				wahoo$ledgeGrabCooldown == 0) {
			ledgeGrab();
		}
		
		if (wahoo$ledgeGrabbing) {
			if (!world.getBlockState(getBlockPos().up().offset(getHorizontalFacing()).up()).isAir() ||
					!voxelShapeEligibleForGrab(world.getBlockState(getBlockPos().up().offset(getHorizontalFacing())).getCollisionShape(world, getBlockPos().up().offset(getHorizontalFacing())))) {
				exitLedgeGrab(true);
			}
			
			setVelocity(0, 0, 0);
			if ((input.jumping || input.pressingForward) && wahoo$ledgeGrabExitCooldown == 0) {
				exitLedgeGrab(false);
			} else if ((isSneaking() || input.pressingBack) && wahoo$ledgeGrabExitCooldown == 0) {
				exitLedgeGrab(true);
			}
		}
		
		// ----- WALL JUMPS -----
		
		// runs on the first tick a player collides with a wall
		if (horizontalCollision) {
			if (wahoo$ticksLeftToWallJump <= 0 && !wahoo$isDiving && !isOnGround() && (
					BingBingWahooClient.CONFIG.allowNormalWallJumps
							? wahoo$previousJumpType.canWallJumpFrom() || wahoo$previousJumpType == JumpTypes.NORMAL
							: wahoo$previousJumpType.canWallJumpFrom())) {
				wahoo$ticksLeftToWallJump = 4;
			}
		}
		
		// this is ugly but it works
		if (wahoo$ticksLeftToWallJump > 0 && !wahoo$isDiving && input.jumping && !wahoo$jumpHeldSinceLastJump && (
				BingBingWahooClient.CONFIG.allowNormalWallJumps
						? wahoo$previousJumpType.canWallJumpFrom() || wahoo$previousJumpType == JumpTypes.NORMAL
						: wahoo$previousJumpType.canWallJumpFrom()) && !getAbilities().flying && !isTouchingWater()) {
			wallJump();
		}
		
		if (wahoo$wallJumping && isOnGround()) {
			exitWallJump();
		}
		
		// ----- GROUND POUND -----
		
		if (BingBingWahooClient.CONFIG.groundPoundType.enabled() && !isOnGround() && isSneaking() && !lastSneaking && !wahoo$longJumping && !getAbilities().flying && !riding && !wahoo$lastRiding && !isTouchingWater() && !world.getBlockState(getBlockPos().down()).isSolidBlock(world, getBlockPos().down())) {
			List<Entity> entities = world.getOtherEntities(this, new Box(getBlockPos()).expand(1.5, 1, 1.5));
			boolean canPound = entities.size() == 0;
			if (canPound) {
				groundPound();
			}
		}
		
		if (wahoo$isGroundPounding) {
			setPose(EntityPose.CROUCHING);
			wahoo$hasGroundPounded = true;
			wahoo$ticksInAirDuringGroundPound++;
			if (wahoo$incipientGroundPound) {
				setVelocity(0, 0, 0);
			}
			
			if (wahoo$incipientGroundPound && wahoo$flipTimer == 0) {
				wahoo$incipientGroundPound = false;
				if (BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0) {
					setPitch(0);
				}
			}
			
			if (!wahoo$incipientGroundPound) {
				if (wahoo$ticksInAirDuringGroundPound > 6) {
					setVelocity(0, -0.5 * wahoo$groundPoundSpeedMultiplier, 0);
					if (wahoo$groundPoundSpeedMultiplier < 4) {
						wahoo$groundPoundSpeedMultiplier = wahoo$groundPoundSpeedMultiplier + 0.5;
					}
				}
			}
			
			if (wahoo$hasGroundPounded && !wahoo$incipientGroundPound && wahoo$lastPos.equals(getBlockPos())) {
				wahoo$ticksGroundPounded++;
				if (wahoo$ticksGroundPounded > 5) {
					exitGroundPound();
				}
			}
			
			if (wahoo$hasGroundPounded && !wahoo$incipientGroundPound && wahoo$ticksGroundPounded > 0 && !wahoo$lastPos.equals(getBlockPos())) {
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
	}
	
	public boolean voxelShapeEligibleForGrab(VoxelShape shape) {
		double xMin = shape.getMin(Direction.Axis.X);
		double xMax = shape.getMax(Direction.Axis.X);
		
		double yMin = shape.getMin(Direction.Axis.Y);
		double yMax = shape.getMax(Direction.Axis.Y);
		
		double zMin = shape.getMin(Direction.Axis.Z);
		double zMax = shape.getMax(Direction.Axis.Z);
		
		double sixteenth = 1f / 16;
		
		// this is unholy
		// iterate over each pixel of a 16x16x16 block, checking for a solid row across which can be grabbed.
		int solidPixelsInARow = 0;
		return switch (getHorizontalFacing()) {
			case NORTH -> {
				// x+, z-
				for (double y = yMax; y > yMin; y -= sixteenth) {
					for (double x = xMin; x < xMax; x += sixteenth) {
						for (double z = zMax; z >= zMin; z -= sixteenth) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			case EAST -> {
				// x+, z+
				for (double y = yMax; y > yMin; y -= sixteenth) {
					for (double x = xMin; x < xMax; x += sixteenth) {
						for (double z = zMin; z < zMax; z += sixteenth) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			case SOUTH -> {
				// x-, z+
				for (double y = yMax; y > yMin; y -= sixteenth) {
					for (double x = xMax; x >= xMin; x -= sixteenth) {
						for (double z = zMin; z < zMax; z += sixteenth) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			case WEST -> {
				// x-, z-
				for (double y = yMax; y > yMin; y -= sixteenth) {
					for (double x = xMax; x >= xMin; x -= sixteenth) {
						for (double z = zMax; z >= zMin; z -= sixteenth) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			default -> throw new RuntimeException("this should never be called, if it did something has gone catastrophically wrong");
		};
	}
	
	/**
	 * Handles triggering of jump-based physics
	 */
	@Override
	public void jump() {
		if (wahoo$isDiving) {
			addVelocity(0, 0.5, 0);
			wahoo$diveFlip = true;
			wahoo$flipTimer = 15;
			setVelocity(getVelocity().multiply(1.25, 1, 1.25));
			wahoo$previousJumpType = JumpTypes.NORMAL;
			return;
		}
		
		if (wahoo$bonked) {
			return;
		}
		
		super.jump();
		if (input.jumping) {
			if ((isOnGround())) {
				if (((BingBingWahooClient.CONFIG.rapidFireLongJumps && (boolean) BingBingWahooClient.GAME_RULES.get(RAPID_FIRE_LONG_JUMPS_RULE.getName())) || wahoo$ticksLeftToLongJump > 0) && (isSneaking() || lastSneaking) && wahoo$previousJumpType.canLongJumpFrom() && (isSprinting() || isWalking() || input.pressingBack)) {
					longJump();
				} else if (wahoo$ticksLeftToDoubleJump > 0 && !wahoo$jumpHeldSinceLastJump && wahoo$previousJumpType == JumpTypes.NORMAL) {
					doubleJump();
				} else if (wahoo$ticksLeftToTripleJump > 0 && wahoo$ticksLeftToTripleJump < 5 && wahoo$previousJumpType == JumpTypes.DOUBLE && (isSprinting() || isWalking() || input.pressingBack)) {
					tripleJump();
				} else if (isSneaking() && getVelocity().getX() == 0 && getVelocity().getZ() == 0 && !wahoo$jumpHeldSinceLastJump && BingBingWahooClient.CONFIG.backFlips) {
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
		if (isSneaking() != lastSneaking) {
			wahoo$ticksSinceSneakingChanged = 0;
		}
		wahoo$ticksSinceSneakingChanged++;
		if (wahoo$ticksSinceSneakingChanged == 1 && isSneaking()) {
			wahoo$ticksLeftToLongJump = 10;
		}
		if (wahoo$ticksLeftToLongJump > 0) {
			wahoo$ticksLeftToLongJump--;
		}
		
		// wall jump
		if (wahoo$ticksLeftToWallJump > 0) {
			wahoo$ticksLeftToWallJump--;
			
			if (wahoo$ticksLeftToWallJump == 0 && wahoo$previousJumpType != JumpTypes.NORMAL && !wahoo$wallJumping && !wahoo$ledgeGrabbing && !isCreative() && wahoo$bonkCooldown == 0 && !(Math.abs(getVelocity().getX()) < 0.07 && Math.abs(getVelocity().getY()) < 0.07)) {
				Direction looking = Direction.fromRotation(getYaw());
				if (world.getBlockState(getBlockPos().offset(looking)).isSolidBlock(world, getBlockPos().offset(looking)) &&
						world.getBlockState(getBlockPos().offset(looking).up()).isSolidBlock(world, getBlockPos().offset(looking).up())) {
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
		
		// dive
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
	protected void updatePose() {
		if (wahoo$isDiving || wahoo$bonked || wahoo$isGroundPounding) {
			return;
		}
		super.updatePose();
	}
	
	@Override
	public void setPose(EntityPose pose) {
		if (wahoo$isGroundPounding) {
			return;
		}
		super.setPose(pose);
	}
	
	/**
	 * Similar to {@link ClientPlayerEntityMixin#updatePose}, allows for special handling of pitch changes
	 */
	@Override
	public void setPitch(float pitch) {
		float tickDelta = MinecraftClient.getInstance().getTickDelta();
		if (wahoo$isBackFlipping && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
			if (BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setPitchRaw(-(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooClient.CONFIG.flipSpeedMultiplier * -24) - 90);
			} else {
				super.setPitch(pitch);
			}
			return;
		}
		
		if (wahoo$midTripleJump && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
			if (BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setPitchRaw(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooClient.CONFIG.flipSpeedMultiplier * -24);
			} else {
				super.setPitch(pitch);
			}
			if (wahoo$flipTimer == 0) {
				exitTripleJump();
			}
			return;
		}
		
		if (wahoo$diveFlip) {
			if (wahoo$flipTimer == 0) {
				exitDive();
			}
			
			if (MinecraftClient.getInstance().options.getPerspective().isFirstPerson() && BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setPitchRaw(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooClient.CONFIG.flipSpeedMultiplier * -24);
			}
			return;
		}
		
		if (wahoo$bonked) {
			return;
		}
		
		if (wahoo$isGroundPounding && wahoo$incipientGroundPound) {
			if (MinecraftClient.getInstance().options.getPerspective().isFirstPerson() && BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0) {
				((EntityAccessor) this).setPitchRaw(Math.max(0, wahoo$flipTimer - tickDelta) * BingBingWahooClient.CONFIG.flipSpeedMultiplier * -24);
			}
			return;
		}
		
		super.setPitch(pitch);
	}
	
	/**
	 * Similar to {@link ClientPlayerEntityMixin#updatePose}, allows for special handling of yaw changes
	 */
	public void setYaw(float yaw) {
		if (wahoo$bonked || wahoo$ledgeGrabbing) {
			return;
		}
		super.setYaw(yaw);
	}
	
	// ---------- JUMPS ----------
	
	public void longJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		// ------- warning: black magic wizardry below -------
		Vec2f velocity = new Vec2f((float) getVelocity().getX(), (float) getVelocity().getZ());
		Vec2f rotation = new Vec2f((float) getRotationVector().getX(), (float) getRotationVector().getZ());
		
		double cosOfVecs = (rotation.dot(velocity)) / (rotation.length() * velocity.length());
		double degreesDiff = Math.toDegrees(Math.acos(cosOfVecs));
		// ----------- end of black magic wizardry -----------
		
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
		
		double multiplier = Math.min(BingBingWahooClient.CONFIG.longJumpSpeedMultiplier, (double) BingBingWahooClient.GAME_RULES.get(LONG_JUMP_SPEED_MULTIPLIER_RULE.getName()));
		double maxSpeed = Math.min(BingBingWahooClient.CONFIG.maxLongJumpSpeed, (double) BingBingWahooClient.GAME_RULES.get(MAX_LONG_JUMP_SPEED_RULE.getName()));
		
		if (degreesDiff > 170) { // BLJ
			if ((boolean) BingBingWahooClient.GAME_RULES.get(BACKWARDS_LONG_JUMPS_RULE.getName()) && BingBingWahooClient.CONFIG.blj) {
				newVelXAbs = Math.abs(getVelocity().getX()) * multiplier;
				newVelZAbs = Math.abs(getVelocity().getZ()) * multiplier;
			} else {
				wahoo$ticksLeftToLongJump = 0;
				wahoo$previousJumpType = JumpTypes.NORMAL;
				return;
			}
		} else {
			newVelXAbs = Math.min(Math.abs(getVelocity().getX()) * multiplier, maxSpeed);
			newVelZAbs = Math.min(Math.abs(getVelocity().getZ()) * multiplier, maxSpeed);
		}
		
		double newVelX = Math.copySign(newVelXAbs, getVelocity().getX());
		double newVelZ = Math.copySign(newVelZAbs, getVelocity().getZ());
		
		// todo: see https://github.com/n64decomp/sm64/blob/ecd3d152fb7c6f658d18543c0f4e8147b50d5dde/src/game/mario.c#L863
		
		setVelocity(newVelX, Math.min(getVelocity().getY() * 1.5, 1), newVelZ);
		wahoo$ticksLeftToLongJump = 0;
		wahoo$longJumping = true;
		wahoo$previousJumpType = JumpTypes.LONG;
	}
	
	private void exitLongJump() {
		wahoo$longJumping = false;
	}
	
	private void doubleJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		setVelocity(getVelocity().multiply(1, 1.75, 1));
		wahoo$ticksLeftToDoubleJump = 0;
		wahoo$previousJumpType = JumpTypes.DOUBLE;
	}
	
	private void tripleJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		setVelocity(getVelocity().multiply(1, 2.5, 1));
		wahoo$ticksLeftToTripleJump = 0;
		wahoo$previousJumpType = JumpTypes.TRIPLE;
		wahoo$midTripleJump = true;
		wahoo$flipTimer = 40;
	}
	
	public void exitTripleJump() {
		wahoo$midTripleJump = false;
		wahoo$tripleJumpTicks = 0;
		wahoo$flipTimer = 0;
		if (BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0 && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
			setPitch(0);
		}
	}
	
	private void dive() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		if (wahoo$midTripleJump) exitTripleJump();
		if (wahoo$wallJumping) exitWallJump();
		
		setPos(getPos().getX(), getPos().getY() + 1, getPos().getZ());
		wahoo$isDiving = true;
		setPose(EntityPose.SWIMMING);
		wahoo$currentDivingVelocity = new Vec3d(
				Math.copySign(Math.min(1.5, Math.abs(getVelocity().getX()) * 2.25), getVelocity().getX()),
				getVelocity().getY(),
				Math.copySign(Math.min(1.5, Math.abs(getVelocity().getZ()) * 2.25), getVelocity().getZ())
		);
		setVelocity(wahoo$currentDivingVelocity.add(0, 0.5, 0));
		wahoo$diveCooldown = 20;
		ClientPlayNetworking.send(BingBingWahoo.DIVE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(true)).writeBlockPos(getBlockPos()));
	}
	
	public void exitDive() {
		wahoo$isDiving = false;
		wahoo$diveFlip = false;
		wahoo$flipTimer = 0;
		if (BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0) setPitch(0);
		
		ClientPlayNetworking.send(BingBingWahoo.DIVE_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(false)));
	}
	
	public void bonk() {
		if (!wahoo$canWahoo || wahoo$wasRiding || !BingBingWahooClient.CONFIG.bonking) return;
		((KeyboardInputExtensions) input).disableControl();
		if (wahoo$isDiving) exitDive();
		if (wahoo$midTripleJump) {
			exitTripleJump();
		}
		
		if (wahoo$wallJumping) {
			exitWallJump();
		}
		
		setVelocity(-getVelocity().getX(), getVelocity().getY(), -getVelocity().getZ());
		setPose(EntityPose.SLEEPING);
		setPitch(-90);
		wahoo$bonked = true;
		wahoo$bonkTime = 30;
		wahoo$bonkCooldown = 20;
//		ClientPlayNetworking.send(BONK_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(true)).writeUuid(getUuid()));
	}
	
	public void exitBonk() {
		((KeyboardInputExtensions) input).enableControl();
		wahoo$bonked = false;
		wahoo$bonkTime = 0;
		setPitch(0);
//		ClientPlayNetworking.send(BONK_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(false)).writeUuid(getUuid()));
	}
	
	private void wallJump() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		if (wahoo$midTripleJump) exitTripleJump();
		
		wahoo$wallJumping = true;
		wahoo$ticksLeftToWallJump = 0;
		wahoo$previousJumpType = JumpTypes.WALL;
		Direction directionOfNearestWall = Direction.UP;
		double distanceToNearestWall = 1;
		for (Direction direction : BingBingWahoo.CARDINAL_DIRECTIONS) {
			BlockState adjacentState = world.getBlockState(getBlockPos().offset(direction));
			if (!adjacentState.isAir()) {
				double distance = getPos().distanceTo(Vec3d.ofCenter(getBlockPos().offset(direction)));
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
		
		Vec3d directionToGo = Vec3d.of(directionOfNearestWall.getOpposite().getVector());
		this.setVelocity(directionToGo.getX() / 2, 0.75, directionToGo.getZ() / 2);
	}
	
	private void exitWallJump() {
		wahoo$wallJumping = false;
	}
	
	private void ledgeGrab() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		if (wahoo$midTripleJump) exitTripleJump();
		if (wahoo$isDiving) exitDive();
		if (wahoo$wallJumping) exitWallJump();
		
		setYaw(getHorizontalFacing().asRotation());
		wahoo$ledgeGrabbing = true;
		wahoo$ledgeGrabExitCooldown = 10;
		wahoo$bonkCooldown = 20;
	}
	
	private void exitLedgeGrab(boolean fall) {
		wahoo$ledgeGrabbing = false;
		wahoo$ledgeGrabCooldown = 20;
		if (!fall) {
			setVelocity(0, 0.75, 0);
		}
	}
	
	private void groundPound() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		if (wahoo$midTripleJump) exitTripleJump();
		if (wahoo$wallJumping) exitWallJump();
		
		wahoo$isGroundPounding = true;
		wahoo$incipientGroundPound = true;
		wahoo$flipTimer = 15;
		ClientPlayNetworking.send(GROUND_POUND_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(true)
				.writeBoolean(BingBingWahooClient.CONFIG.groundPoundType == GroundPoundTypes.DESTRUCTIVE)));
	}
	
	private void exitGroundPound() {
		wahoo$isGroundPounding = false;
		wahoo$hasGroundPounded = false;
		wahoo$ticksInAirDuringGroundPound = 0;
		wahoo$flipTimer = 0;
		wahoo$ticksGroundPounded = 0;
		ClientPlayNetworking.send(GROUND_POUND_PACKET, new PacketByteBuf(PacketByteBufs.create().writeBoolean(false)
				.writeBoolean(BingBingWahooClient.CONFIG.groundPoundType == GroundPoundTypes.DESTRUCTIVE)));
	}
	
	private void backFlip() {
		if (!wahoo$canWahoo || wahoo$wasRiding) return;
		wahoo$isBackFlipping = true;
		wahoo$flipTimer = 20;
		float x = -MathHelper.sin(getYaw() * (float) (Math.PI / 180.0)) * MathHelper.cos(getPitch() * (float) (Math.PI / 180.0));
		float z = MathHelper.cos(getYaw() * (float) (Math.PI / 180.0)) * MathHelper.cos(getPitch() * (float) (Math.PI / 180.0));
		this.setVelocity(-x * 0.5, 1, -z * 0.5);
		wahoo$previousJumpType = JumpTypes.BACK_FLIP;
	}
	
	private void exitBackFlip() {
		wahoo$flipTimer = 0;
		wahoo$isBackFlipping = false;
		if (BingBingWahooClient.CONFIG.flipSpeedMultiplier != 0) setPitch(0);
	}
}
