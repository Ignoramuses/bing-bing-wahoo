package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.cap.CapWearer;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;
import static net.ignoramuses.bingBingWahoo.BingBingWahoo.TRINKETS_LOADED;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements CapWearer {
	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);
	
	@Shadow
	public abstract void equipStack(EquipmentSlot slot, ItemStack stack);
	
	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}
	
	@Override
	public boolean isWearingCap() {
		ItemStack head = getEquippedStack(EquipmentSlot.HEAD);
		if ((head == null || head.isEmpty() || !head.isOf(MYSTERIOUS_CAP)) && TRINKETS_LOADED) {
			head = TrinketsHandler.getCapStack((LivingEntity) (Object) this);
		}
		return head != null && head.isOf(MYSTERIOUS_CAP);
	}
	
	@Override
	public ItemStack getCap() {
		ItemStack head = getEquippedStack(EquipmentSlot.HEAD);
		if ((head == null || head.isEmpty() || !head.isOf(MYSTERIOUS_CAP)) && TRINKETS_LOADED) {
			head = TrinketsHandler.getCapStack((LivingEntity) (Object) this);
		}
		return head != null ? head : ItemStack.EMPTY;
	}
	
	@Inject(at = @At("HEAD"), method = {"damageHelmet", "damageArmor"})
	private void wahoo$damageCap(DamageSource source, float amount, CallbackInfo ci) {
		if (isWearingCap()) {
			getCap().damage((int) amount, (LivingEntity) (Object) this, (entity) -> {
				if (entity instanceof PlayerEntity player) {
					player.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, 3)); // 3: head slot index
				}
				entity.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
			});
		}
	}
	
	@Inject(at = @At("HEAD"), method = "dropEquipment")
	private void wahoo$dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
		if (isWearingCap()) {
			dropStack(getCap());
		}
	}
	
//	@Override
//	public ActionResult interact(PlayerEntity player, Hand hand) {
//		ItemStack held = player.getStackInHand(hand);
//		if (held.isOf(MYSTERIOUS_CAP) && !this.isSpectator()) {
//			if (!player.isCreative()) {
//				player.setStackInHand(hand, ItemStack.EMPTY);
//			}
//			if (isWearingCap()) {
//				dropStack(getCap());
//			}
//			equipStack(EquipmentSlot.HEAD, held.copy());
//			SoundEvent soundEvent = held.getEquipSound();
//			if (soundEvent != null) {
//				this.emitGameEvent(GameEvent.EQUIP);
//				this.playSound(soundEvent, 1.0F, 1.0F);
//			}
//			return ActionResult.SUCCESS;
//		}
//		return super.interact(player, hand);
//	}
}
