package io.github.ignoramuses.bing_bing_wahoo.mixin;

import io.github.ignoramuses.bing_bing_wahoo.content.cap.CapWearer;
import io.github.ignoramuses.bing_bing_wahoo.compat.TrinketsCompat;
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

import static io.github.ignoramuses.bing_bing_wahoo.WahooRegistry.MYSTERIOUS_CAP;

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
		if ((head == null || head.isEmpty() || !head.is(MYSTERIOUS_CAP))) {
			head = TrinketsCompat.getCapTrinketStack((LivingEntity) (Object) this);
		}
		return head != null && head.is(MYSTERIOUS_CAP);
	}
	
	@Override
	public ItemStack getCap() {
		ItemStack head = getItemBySlot(EquipmentSlot.HEAD);
		if ((head == null || head.isEmpty() || !head.is(MYSTERIOUS_CAP))) {
			head = TrinketsCompat.getCapTrinketStack((LivingEntity) (Object) this);
		}
		return head != null ? head : ItemStack.EMPTY;
	}
	
	@Inject(at = @At("HEAD"), method = {"hurtHelmet", "hurtArmor"})
	private void damageCap(DamageSource source, float amount, CallbackInfo ci) {
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
