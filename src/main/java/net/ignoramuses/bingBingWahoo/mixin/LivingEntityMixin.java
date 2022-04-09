package net.ignoramuses.bingBingWahoo.mixin;

import net.ignoramuses.bingBingWahoo.cap.CapWearer;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
	public abstract void setItemSlot(EquipmentSlot slot, ItemStack stack);
	
	@Shadow
	public abstract ItemStack getItemBySlot(EquipmentSlot slot);
	
	public LivingEntityMixin(EntityType<?> type, Level world) {
		super(type, world);
	}
	
	@Override
	public boolean isWearingCap() {
		ItemStack head = getItemBySlot(EquipmentSlot.HEAD);
		if ((head == null || head.isEmpty() || !head.is(MYSTERIOUS_CAP)) && TRINKETS_LOADED) {
			head = TrinketsHandler.getCapStack((LivingEntity) (Object) this);
		}
		return head != null && head.is(MYSTERIOUS_CAP);
	}
	
	@Override
	public ItemStack getCap() {
		ItemStack head = getItemBySlot(EquipmentSlot.HEAD);
		if ((head == null || head.isEmpty() || !head.is(MYSTERIOUS_CAP)) && TRINKETS_LOADED) {
			head = TrinketsHandler.getCapStack((LivingEntity) (Object) this);
		}
		return head != null ? head : ItemStack.EMPTY;
	}
	
	@Inject(at = @At("HEAD"), method = {"hurtHelmet", "hurtArmor"})
	private void wahoo$damageCap(DamageSource source, float amount, CallbackInfo ci) {
		if (isWearingCap()) {
			getCap().hurtAndBreak((int) amount, (LivingEntity) (Object) this, (entity) -> {
				if (entity instanceof Player player) {
					player.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, 3)); // 3: head slot index
				}
				entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
			});
		}
	}
}
