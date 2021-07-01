package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.JumpTypes;
import net.ignoramuses.bingBingWahoo.ServerPlayerEntityExtensions;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityExtensions {
	@Unique
	private JumpTypes wahoo$previousJumpType = JumpTypes.NORMAL;
	@Unique
	private boolean wahoo$groundPounding = false;
	@Unique
	private long wahoo$ticksGroundPoundingFor = 0;
	@Unique
	private final BlockPos.Mutable wahoo$groundPoundBlockBreakPos = new BlockPos.Mutable();
	@Unique
	private BlockPos.Mutable wahoo$groundPoundStartPos = new BlockPos.Mutable();
	
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}
	
	@Inject(at = @At("HEAD"), method = "tick()V")
	public void wahoo$tick(CallbackInfo ci) {
		if (wahoo$groundPounding) {
			wahoo$ticksGroundPoundingFor++;
			if (isOnGround()) {
				for (int x = (int) Math.floor(getBoundingBox().minX); x <= Math.floor(getBoundingBox().maxX); x++) {
					for (int z = (int) Math.floor(getBoundingBox().minZ); z <= Math.floor(getBoundingBox().maxZ); z++) {
						wahoo$groundPoundBlockBreakPos.set(x, getBlockPos().down().getY(), z);
						BlockState state = world.getBlockState(wahoo$groundPoundBlockBreakPos);
						if ((state.getHardness(world, wahoo$groundPoundBlockBreakPos) <= 0.5 && !state.isOf(Blocks.NETHERRACK)) || state.isOf(Blocks.BRICKS)) {
							world.breakBlock(wahoo$groundPoundBlockBreakPos, true, this);
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
		} else {
			fallDistance -= switch (wahoo$previousJumpType) {
				case DOUBLE, LONG -> 4;
				case BACK_FLIP -> 6;
				case TRIPLE -> 7;
				default -> 0;
			};
		}
		return super.computeFallDamage(fallDistance, damageMultiplier);
	}
	
	@Override
	public void setPreviousJumpType(JumpTypes type) {
		wahoo$previousJumpType = type;
	}
	
	@Override
	public void setGroundPounding(boolean value) {
		wahoo$groundPounding = value;
		if (value) {
			wahoo$groundPoundStartPos.set(getBlockPos());
		} else {
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
						entity.damage(DamageSource.ANVIL, damage);
					}
				}
			}
			wahoo$ticksGroundPoundingFor = 0;
		}
	}
}
