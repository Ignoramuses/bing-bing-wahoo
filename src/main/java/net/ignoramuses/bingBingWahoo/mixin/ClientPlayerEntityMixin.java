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

import static net.ignoramuses.bingBingWahoo.BingBingWahooClient.LONG_JUMP_SPEED_MULTIPLIER;
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
	
	@Shadow public float renderYaw;
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
	
	@Inject(at = @At("HEAD"), method = "sendMovementPackets()V")
	public void wahoo$sendMovementPackets(CallbackInfo ci) {
	
	}
	
	@Inject(at = @At("RETURN"), method = "tickMovement()V")
	public void wahoo$tickMovement(CallbackInfo ci) {
		updateJumpTicks();
	}
	
	@Override
	public void jump() {
		super.jump();
		if (input.jumping) {
			if ((isOnGround())) {
				if ((isSneaking() || lastSneaking) && (BingBingWahooClient.rapidFire || wahoo$ticksLeftToLongJump > 0) && (wahoo$previousJumpType == JumpTypes.NORMAL || wahoo$previousJumpType == JumpTypes.LONG)) {
					longJump();
				} else if ((wahoo$ticksLeftToDoubleJump > 0) && wahoo$previousJumpType == JumpTypes.NORMAL) {
					doubleJump();
				} else {
					wahoo$previousJumpType = JumpTypes.NORMAL;
				}
			}
		}
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
		
		double xToZRatio = velXAbs / velZAbs;
		
		// BLJ :)
		// special handling for axis
		if (velXAbs == 0 || velZAbs == 0) {
			if (degreesDiff > 170) {
				newVelXAbs = velXAbs * LONG_JUMP_SPEED_MULTIPLIER * 2;
				newVelZAbs = velZAbs * LONG_JUMP_SPEED_MULTIPLIER * 2;
			} else {
				newVelXAbs = Math.min(velXAbs * LONG_JUMP_SPEED_MULTIPLIER * 2, MAX_LONG_JUMP_SPEED);
				newVelZAbs = Math.min(velZAbs * LONG_JUMP_SPEED_MULTIPLIER * 2, MAX_LONG_JUMP_SPEED);
			}
		} else {
			if (degreesDiff > 170) {
				newVelXAbs = velZAbs * LONG_JUMP_SPEED_MULTIPLIER * xToZRatio * 2;
				newVelZAbs = velZAbs * LONG_JUMP_SPEED_MULTIPLIER * 2;
			} else {
				newVelXAbs = Math.min(velZAbs * LONG_JUMP_SPEED_MULTIPLIER * xToZRatio * 2, MAX_LONG_JUMP_SPEED);
				newVelZAbs = Math.min(velZAbs * LONG_JUMP_SPEED_MULTIPLIER * 2, MAX_LONG_JUMP_SPEED);
			}
		}
		
		double newVelX = Math.copySign(newVelXAbs, velX);
		double newVelZ = Math.copySign(newVelZAbs, velZ);
		
		setVelocity(newVelX, Math.min(velY * 1.5, 1), newVelZ);
		// ----------- end of black magic wizardry -----------
		wahoo$ticksLeftToLongJump = 0;
		wahoo$previousJumpType = JumpTypes.LONG;
	}
	
	private void doubleJump() {
		setVelocity(getVelocity().multiply(1, 1.75, 1));
		wahoo$ticksLeftToDoubleJump = 0;
		wahoo$previousJumpType = JumpTypes.DOUBLE;
	}
	
	private void updateJumpTicks() {
		// double jump
		if (!lastOnGround && isOnGround()) {
			wahoo$ticksLeftToDoubleJump = 5;
		}
		if (wahoo$ticksLeftToDoubleJump > 0) {
			--this.wahoo$ticksLeftToDoubleJump;
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
