package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.WahooUtils;
import net.ignoramuses.bingBingWahoo.WahooUtils.PlayerEntityExtensions;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraft.entity.Entity.RemovalReason.CHANGED_DIMENSION;
import static net.minecraft.entity.Entity.RemovalReason.DISCARDED;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityExtensions {
	@Unique
	private boolean wahoo$isBonked = false;

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}
	
	@Override
	public void wahoo$setBonked(boolean value, UUID bonked) {
		wahoo$isBonked = value;
	}
	
	@Inject(at = @At("HEAD"), method = "updatePose()V", cancellable = true)
	protected void wahoo$updatePose(CallbackInfo ci) {
		if (wahoo$isBonked) {
			setPose(EntityPose.SLEEPING);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
	private void wahoo$readCaps(NbtCompound nbt, CallbackInfo ci) {
		if (nbt.contains("Caps")) {
			NbtList caps = nbt.getList("Caps", NbtElement.COMPOUND_TYPE);
			for (NbtElement element : caps) {
				NbtCompound data = (NbtCompound) element;
				FlyingCapEntity cap = BingBingWahoo.FLYING_CAP.create(world);
				cap.readNbt(data);
				world.spawnEntity(cap);
				FlyingCapEntity.BY_PLAYER.computeIfAbsent(getUuidAsString(), $ -> new ArrayList<>()).add(cap);
			}
		}
	}
	
	@Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
	private void wahoo$writeCaps(NbtCompound nbt, CallbackInfo ci) {
		List<FlyingCapEntity> entities = FlyingCapEntity.BY_PLAYER.get(getUuidAsString());
		if (entities != null) {
			NbtList caps = new NbtList();
			for (FlyingCapEntity cap : entities) {
				NbtCompound data = new NbtCompound();
				cap.writeNbt(data);
				caps.add(data);
			}
			nbt.put("Caps", caps);
		}
		
	}
	
	@Inject(at = @At("TAIL"), method = "remove")
	private void wahoo$onRemove(RemovalReason reason, CallbackInfo ci) {
		if (reason == DISCARDED) return; // don't want to catch respawning
		List<FlyingCapEntity> entities = FlyingCapEntity.BY_PLAYER.get(getUuidAsString());
		if (entities != null) {
			entities.forEach(cap -> cap.remove(reason == CHANGED_DIMENSION ? reason : RemovalReason.UNLOADED_WITH_PLAYER)); // prevent CME
			entities.clear();
		}
	}
	
	@Inject(at = @At("TAIL"), method = "tick")
	private void wahoo$tick(CallbackInfo ci) {
		List<FlyingCapEntity> caps = FlyingCapEntity.BY_PLAYER.get(getUuidAsString());
		if (caps != null) {
			for (FlyingCapEntity cap : caps) {
				if (cap.world != this.world) {
					world.spawnEntity(cap);
				}
			}
		}
	}
	
	@Override
	public void setPose(EntityPose pose) {
		if (wahoo$isBonked) {
			super.setPose(EntityPose.SLEEPING);
			return;
		}
		super.setPose(pose);
	}
}
