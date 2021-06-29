package net.ignoramuses.bingBingWahoo.mixin;

import com.mojang.authlib.GameProfile;
import net.ignoramuses.bingBingWahoo.JumpTypes;
import net.ignoramuses.bingBingWahoo.ServerPlayerEntityExtensions;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
	private
	BlockPos.Mutable wahoo$groundPoundPos = new BlockPos.Mutable();
	
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}
	
	@Inject(at = @At("HEAD"), method = "tick()V")
	public void wahoo$tick(CallbackInfo ci) {
		if (wahoo$groundPounding && isOnGround()) {
			for (int x = (int) Math.floor(getBoundingBox().minX); x <= Math.floor(getBoundingBox().maxX); x++) {
				for (int z = (int) Math.floor(getBoundingBox().minZ); z <= Math.floor(getBoundingBox().maxZ); z++) {
					wahoo$groundPoundPos.set(x, getBlockPos().down().getY(), z);
					BlockState state = world.getBlockState(wahoo$groundPoundPos);
					if (state.getHardness(world, wahoo$groundPoundPos) <= 0.5 && !state.isOf(Blocks.NETHERRACK)) {
						world.breakBlock(wahoo$groundPoundPos, true, this);
					}
				}
			}
		}
	}
	
	@Override
	protected int computeFallDamage(float fallDistance, float damageMultiplier) {
		if (wahoo$previousJumpType == JumpTypes.DOUBLE) {
			fallDistance -= 4;
		} else if (wahoo$previousJumpType == JumpTypes.BACK_FLIP) {
			fallDistance -= 6;
		} else if (wahoo$previousJumpType == JumpTypes.TRIPLE) {
			fallDistance -= 7;
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
	}
}
