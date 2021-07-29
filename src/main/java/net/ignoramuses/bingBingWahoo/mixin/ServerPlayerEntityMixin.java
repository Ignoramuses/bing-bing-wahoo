package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.JumpTypes;
import net.ignoramuses.bingBingWahoo.ServerPlayerEntityExtensions;
import net.ignoramuses.bingBingWahoo.TrinketsHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;
import static net.ignoramuses.bingBingWahoo.BingBingWahoo.TRINKETS_LOADED;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityExtensions {
	@Unique
	private final BlockPos.Mutable wahoo$groundPoundBlockBreakPos = new BlockPos.Mutable();
	@Unique
	private final BlockPos.Mutable wahoo$groundPoundStartPos = new BlockPos.Mutable();
	@Unique
	private final BlockPos.Mutable wahoo$divingStartPos = new BlockPos.Mutable();
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
	private boolean wahoo$wearingGreenCap = false;
	@Unique
	private long wahoo$ticksUntilAbilityApplies = 1;
	@Unique
	private boolean wahoo$destructionPermOverride = false;
	
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}
	
	@Inject(at = @At("HEAD"), method = "tick()V")
	public void wahoo$tick(CallbackInfo ci) {
		wahoo$wearingGreenCap = false;
		if (getEquippedStack(EquipmentSlot.HEAD).isOf(MYSTERIOUS_CAP)) {
			wahoo$wearingGreenCap = BingBingWahoo.MYSTERIOUS_CAP.getColor(getEquippedStack(EquipmentSlot.HEAD)) == 0x80C71F;
		} else if (TRINKETS_LOADED) {
			ItemStack hatStack = TrinketsHandler.getHatStack(this);
			if (hatStack != null) {
				wahoo$wearingGreenCap = BingBingWahoo.MYSTERIOUS_CAP.getColor(hatStack) == 0x80C71F; // luigi number 1!
			}
		}
		
		if (wahoo$ticksUntilAbilityApplies > 0) wahoo$ticksUntilAbilityApplies--;
		if (wahoo$wearingGreenCap) {
			if (wahoo$ticksUntilAbilityApplies == 0) {
				addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, 0, false, false, true));
				wahoo$ticksUntilAbilityApplies = 199;
			}
		}
		
		if (wahoo$groundPounding) {
			wahoo$ticksGroundPoundingFor++;
			if (isOnGround() && wahoo$destructiveGroundPound && (world.getGameRules().getBoolean(BingBingWahoo.DESTRUCTIVE_GROUND_POUND_RULE) || wahoo$destructionPermOverride)) {
				for (int x = (int) Math.floor(getBoundingBox().minX); x <= Math.floor(getBoundingBox().maxX); x++) {
					for (int z = (int) Math.floor(getBoundingBox().minZ); z <= Math.floor(getBoundingBox().maxZ); z++) {
						wahoo$groundPoundBlockBreakPos.set(x, getBlockPos().down().getY(), z);
						if (world.canPlayerModifyAt(this, wahoo$groundPoundBlockBreakPos)) {
							BlockState state = world.getBlockState(wahoo$groundPoundBlockBreakPos);
							if ((state.getHardness(world, wahoo$groundPoundBlockBreakPos) <= 0.5 && !state.isOf(Blocks.NETHERRACK) && !state.isOf(Blocks.BEDROCK)) || state.isOf(Blocks.BRICKS) || state.isOf(Blocks.GRASS_BLOCK)) {
								world.breakBlock(wahoo$groundPoundBlockBreakPos, true, this);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	protected int computeFallDamage(float fallDistance, float damageMultiplier) {
		if (wahoo$groundPounding) {
			fallDistance = wahoo$groundPoundStartPos.subtract(getBlockPos()).getY();
		} else if (wahoo$diving) {
			fallDistance = wahoo$divingStartPos.subtract(getBlockPos()).getY();
		} else {
			fallDistance -= switch (wahoo$previousJumpType) {
				case DOUBLE, LONG -> 4;
				case BACK_FLIP -> 6;
				case TRIPLE -> 7;
				default -> 0;
			};
		}
		
		int fallDamage = super.computeFallDamage(fallDistance, damageMultiplier);
		
		if (fallDamage > getHealth() && wahoo$groundPounding) {
			return (int) (getHealth() - 4);
		}
		
		return fallDamage;
	}
	
	@Override
	public void setPreviousJumpType(JumpTypes type) {
		wahoo$previousJumpType = type;
	}
	
	@Override
	public void setGroundPounding(boolean value, boolean destruction) {
		wahoo$destructiveGroundPound = destruction;
		wahoo$groundPounding = value;
		if (value) {
			wahoo$groundPoundStartPos.set(getBlockPos());
		} else {
			if (destruction) {
				if (wahoo$ticksGroundPoundingFor > 60) {
					world.createExplosion(this, getPos().getX(), getPos().getY(), getPos().getZ(), 2, Explosion.DestructionType.NONE);
				} else {
					Box box = new Box(getPos().getX() - 1, getPos().getY() - 1, getPos().getZ() - 1, getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
					int damage = wahoo$ticksGroundPoundingFor >= 15
							? wahoo$ticksGroundPoundingFor >= 30
							? wahoo$ticksGroundPoundingFor >= 45
							? 20
							: 10
							: 5
							: 0;
					
					if (wahoo$ticksGroundPoundingFor >= 15) {
						for (Entity entity : world.getOtherEntities(this, box)) {
							if (!(entity instanceof ItemEntity)) {
								entity.damage(DamageSource.ANVIL, damage);
							}
						}
					}
				}
			}
			wahoo$ticksGroundPoundingFor = 0;
		}
	}
	
	public void setDiving(boolean value, @Nullable BlockPos startPos) {
		if (value) {
			setPose(EntityPose.SWIMMING);
			wahoo$divingStartPos.set(startPos);
		}
		wahoo$diving = value;
	}
	
	@Override
	protected void updatePose() {
		if (wahoo$diving) {
			return;
		}
		super.updatePose();
	}
	
	@Override
	public void setPose(EntityPose pose) {
		if (wahoo$diving) {
			return;
		}
		super.setPose(pose);
	}
	
	@Override
	public void setDestructionPermOverride(boolean value) {
		wahoo$destructionPermOverride = value;
	}
}
