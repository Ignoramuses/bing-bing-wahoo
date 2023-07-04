package io.github.ignoramuses.bing_bing_wahoo.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.extensions.PlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.extensions.ServerPlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.JumpType;
import io.github.ignoramuses.bing_bing_wahoo.synced_config.SyncedConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
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

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ServerPlayerExtensions, PlayerExtensions {
	@Shadow
	public abstract void teleportTo(double x, double y, double z);

	@Shadow public abstract void moveTo(double x, double y, double z);

	@Unique
	private final BlockPos.MutableBlockPos groundPoundBlockBreakPos = new BlockPos.MutableBlockPos();
	@Unique
	private final BlockPos.MutableBlockPos groundPoundStartPos = new BlockPos.MutableBlockPos();
	@Unique
	private final BlockPos.MutableBlockPos divingStartPos = new BlockPos.MutableBlockPos();
	@Unique
	private JumpType previousJumpType = JumpType.NORMAL;
	@Unique
	private boolean groundPounding = false;
	@Unique
	private long ticksGroundPoundingFor = 0;
	@Unique
	private boolean destructiveGroundPound = true;
	@Unique
	private boolean diving = false;
	@Unique
	private boolean destructionPermOverride = false;
	@Unique
	private boolean sliding = false;
	@Unique
	private boolean bonked = false;

	public ServerPlayerMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Override
	public boolean getSliding() {
		return diving || sliding;
	}
	
	@Inject(at = @At("HEAD"), method = "tick()V")
	public void tick(CallbackInfo ci) {
		if (groundPounding) {
			ticksGroundPoundingFor++;
			Level level = level();
			if (onGround() && destructiveGroundPound && (level.getGameRules().getBoolean(SyncedConfig.DESTRUCTIVE_GROUND_POUNDS.ruleKey) || destructionPermOverride)) {
				for (int x = (int) Math.floor(getBoundingBox().minX); x <= Math.floor(getBoundingBox().maxX); x++) {
					for (int z = (int) Math.floor(getBoundingBox().minZ); z <= Math.floor(getBoundingBox().maxZ); z++) {
						groundPoundBlockBreakPos.set(x, blockPosition().below().getY(), z);
						if (level.mayInteract(this, groundPoundBlockBreakPos)) {
							BlockState state = level.getBlockState(groundPoundBlockBreakPos);
							float destroySpeed = state.getDestroySpeed(level, groundPoundBlockBreakPos);
							if (destroySpeed <= 0 || destroySpeed > 0.5) { // unbreakable
								if (!state.is(BingBingWahoo.GROUND_POUND_WHITELIST))
									continue; // if it's not in the whitelist, skip it
							}

							if (state.is(BingBingWahoo.GROUND_POUND_BLACKLIST))
								continue; // skip if blacklisted

							level.destroyBlock(groundPoundBlockBreakPos, true, this);
						}
					}
				}
			}
		}
	}
	
	@Override
	protected boolean isStayingOnGroundSurface() {
		if (previousJumpType == JumpType.LONG) return false;
		return super.isStayingOnGroundSurface();
	}
	
	@Override
	protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
		if (groundPounding) {
			fallDistance = groundPoundStartPos.subtract(blockPosition()).getY();
		} else if (diving) {
			fallDistance = divingStartPos.subtract(blockPosition()).getY();
			divingStartPos.set(blockPosition());
		} else {
			fallDistance -= switch (previousJumpType) {
				case DOUBLE, LONG -> 4;
				case BACK_FLIP -> 6;
				case TRIPLE -> 7;
				default -> 0;
			};
		}
		
		int fallDamage = super.calculateFallDamage(fallDistance, damageMultiplier);
		
		if (fallDamage > getHealth() && groundPounding) {
			return (int) (getHealth() - 1);
		}
		
		return fallDamage;
	}

	@Override
	public void setBonked(boolean value) {
		if (bonked == value)
			return;
		bonked = value;
		if (bonked) {
			moveTo(Vec3.atCenterOf(blockPosition())); // don't suffocate in wall
			setPose(Pose.SLEEPING);
		} else {
			setPose(Pose.STANDING);
		}
	}

	@Override
	public void setPreviousJumpType(JumpType type) {
		previousJumpType = type;
	}
	
	@Override
	public void setGroundPounding(boolean value, boolean destruction) {
		destructiveGroundPound = destruction;
		if (groundPounding == value)
			return;
		groundPounding = value;
		if (groundPounding) {
			groundPoundStartPos.set(blockPosition());
		} else {
			if (destruction) {
				Level level = level();
				Vec3 pos = position();
				if (ticksGroundPoundingFor > 60) {
					level.explode(this, pos.x(), pos.y(), pos.z(), 2, ExplosionInteraction.NONE);
				} else {
					AABB box = new AABB(pos.x() - 1, pos.y() - 1, pos.z() - 1, pos.x() + 1, pos.y() + 1, pos.z() + 1);
					int damage = ticksGroundPoundingFor >= 15
							? ticksGroundPoundingFor >= 30
							? ticksGroundPoundingFor >= 45
							? 20
							: 10
							: 5
							: 0;
					
					if (ticksGroundPoundingFor >= 15) {
						for (Entity entity : level.getEntities(this, box)) {
							if (entity instanceof LivingEntity) {
								DamageSource source = level.damageSources().anvil(this);
								entity.hurt(source, damage);
							}
						}
					}
				}
			}
			ticksGroundPoundingFor = 0;
		}
	}
	
	@Override
	public void setDiving(boolean value, @Nullable BlockPos startPos) {
		if (value) {
			setPose(Pose.SWIMMING);
			divingStartPos.set(startPos);
		} else {
			setPose(Pose.STANDING);
		}
		diving = value;
	}
	
	@Override
	public void setSliding(boolean value) {
		sliding = value;
	}
	
	@Override
	protected void updatePlayerPose() {
		if (diving || bonked || groundPounding) {
			return;
		}
		super.updatePlayerPose();
	}

	@Override
	public void setDestructionPermOverride(boolean value) {
		destructionPermOverride = value;
	}

	@Override
	public boolean isGroundPounding() {
		return groundPounding;
	}

	@Override
	public boolean isDiving() {
		return diving;
	}
}
