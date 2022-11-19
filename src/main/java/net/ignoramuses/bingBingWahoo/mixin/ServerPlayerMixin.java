package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.WahooUtils.PlayerExtensions;
import net.ignoramuses.bingBingWahoo.WahooUtils.ServerPlayerExtensions;
import net.ignoramuses.bingBingWahoo.movement.JumpTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.ignoramuses.bingBingWahoo.WahooCommands.DESTRUCTIVE_GROUND_POUND_RULE;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ServerPlayerExtensions, PlayerExtensions {
	@Shadow
	public abstract void teleportTo(double x, double y, double z);

	@Shadow public abstract void moveTo(double x, double y, double z);

	@Unique
	private final BlockPos.MutableBlockPos wahoo$groundPoundBlockBreakPos = new BlockPos.MutableBlockPos();
	@Unique
	private final BlockPos.MutableBlockPos wahoo$groundPoundStartPos = new BlockPos.MutableBlockPos();
	@Unique
	private final BlockPos.MutableBlockPos wahoo$divingStartPos = new BlockPos.MutableBlockPos();
	@Unique
	private JumpTypes wahoo$previousJumpType = JumpTypes.NORMAL;
	@Unique
	private boolean wahoo$groundPounding = false;
	@Unique
	private long wahoo$ticksGroundPoundingFor = 0;
	@Unique
	private boolean wahoo$destructiveGroundPound = true;
	@Unique
	private boolean wahoo$diving = false;
	@Unique
	private boolean wahoo$destructionPermOverride = false;
	@Unique
	private boolean wahoo$sliding = false;
	@Unique
	private boolean wahoo$bonked = false;

	public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
		super(level, pos, yRot, gameProfile, profilePublicKey);
	}

	public boolean wahoo$getSliding() {
		return wahoo$diving || wahoo$sliding;
	}
	
	@Inject(at = @At("HEAD"), method = "tick()V")
	public void wahoo$tick(CallbackInfo ci) {
		if (wahoo$groundPounding) {
			wahoo$ticksGroundPoundingFor++;
			if (isOnGround() && wahoo$destructiveGroundPound && (level.getGameRules().getBoolean(DESTRUCTIVE_GROUND_POUND_RULE) || wahoo$destructionPermOverride)) {
				for (int x = (int) Math.floor(getBoundingBox().minX); x <= Math.floor(getBoundingBox().maxX); x++) {
					for (int z = (int) Math.floor(getBoundingBox().minZ); z <= Math.floor(getBoundingBox().maxZ); z++) {
						wahoo$groundPoundBlockBreakPos.set(x, blockPosition().below().getY(), z);
						if (level.mayInteract(this, wahoo$groundPoundBlockBreakPos)) {
							BlockState state = level.getBlockState(wahoo$groundPoundBlockBreakPos);
							if ((state.getDestroySpeed(level, wahoo$groundPoundBlockBreakPos) <= 0.5 && !state.is(Blocks.NETHERRACK) && !state.is(Blocks.BEDROCK)) || state.is(Blocks.BRICKS) || state.is(Blocks.GRASS_BLOCK)) {
								level.destroyBlock(wahoo$groundPoundBlockBreakPos, true, this);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	protected boolean isStayingOnGroundSurface() {
		if (wahoo$previousJumpType == JumpTypes.LONG) return false;
		return super.isStayingOnGroundSurface();
	}
	
	@Override
	protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
		if (wahoo$groundPounding) {
			fallDistance = wahoo$groundPoundStartPos.subtract(blockPosition()).getY();
		} else if (wahoo$diving) {
			fallDistance = wahoo$divingStartPos.subtract(blockPosition()).getY();
			wahoo$divingStartPos.set(blockPosition());
		} else {
			fallDistance -= switch (wahoo$previousJumpType) {
				case DOUBLE, LONG -> 4;
				case BACK_FLIP -> 6;
				case TRIPLE -> 7;
				default -> 0;
			};
		}
		
		int fallDamage = super.calculateFallDamage(fallDistance, damageMultiplier);
		
		if (fallDamage > getHealth() && wahoo$groundPounding) {
			return (int) (getHealth() - 1);
		}
		
		return fallDamage;
	}

	@Override
	public void wahoo$setBonked(boolean value) {
		if (value) {
			moveTo(Vec3.atCenterOf(blockPosition())); // don't suffocate in wall
			setPose(Pose.SLEEPING);
		} else {
			setPose(Pose.STANDING);
		}
		wahoo$bonked = value;
	}

	@Override
	public void wahoo$setPreviousJumpType(JumpTypes type) {
		wahoo$previousJumpType = type;
	}
	
	@Override
	public void wahoo$setGroundPounding(boolean value, boolean destruction) {
		wahoo$destructiveGroundPound = destruction;
		wahoo$groundPounding = value;
		if (value) {
			wahoo$groundPoundStartPos.set(blockPosition());
		} else {
			if (destruction) {
				if (wahoo$ticksGroundPoundingFor > 60) {
					level.explode(this, position().x(), position().y(), position().z(), 2, Explosion.BlockInteraction.NONE);
				} else {
					AABB box = new AABB(position().x() - 1, position().y() - 1, position().z() - 1, position().x() + 1, position().y() + 1, position().z() + 1);
					int damage = wahoo$ticksGroundPoundingFor >= 15
							? wahoo$ticksGroundPoundingFor >= 30
							? wahoo$ticksGroundPoundingFor >= 45
							? 20
							: 10
							: 5
							: 0;
					
					if (wahoo$ticksGroundPoundingFor >= 15) {
						for (Entity entity : level.getEntities(this, box)) {
							if (entity instanceof LivingEntity) {
								entity.hurt(DamageSource.ANVIL, damage);
							}
						}
					}
				}
			}
			wahoo$ticksGroundPoundingFor = 0;
		}
	}
	
	@Override
	public void wahoo$setDiving(boolean value, @Nullable BlockPos startPos) {
		if (value) {
			setPose(Pose.SWIMMING);
			wahoo$divingStartPos.set(startPos);
		} else {
			setPose(Pose.STANDING);
		}
		wahoo$diving = value;
	}
	
	@Override
	public void wahoo$setSliding(boolean value) {
		wahoo$sliding = value;
	}
	
	@Override
	protected void updatePlayerPose() {
		if (wahoo$diving || wahoo$bonked || wahoo$groundPounding) {
			return;
		}
		super.updatePlayerPose();
	}

	@Override
	public void wahoo$setDestructionPermOverride(boolean value) {
		wahoo$destructionPermOverride = value;
	}
}
