package io.github.ignoramuses.bing_bing_wahoo.content.cap;

import io.github.ignoramuses.bing_bing_wahoo.compat.TrinketsCompat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public enum PreferredCapSlot {
	// priority order
	TRINKETS {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			return TrinketsCompat.getCapTrinketStack(entity) == null;
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			TrinketsCompat.equipInHatTrinketSlot(entity, stack);
		}
	},
	HEAD {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			return entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			entity.setItemSlot(EquipmentSlot.HEAD, stack);
		}
	},
	MAIN_HAND {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			return entity.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			entity.setItemInHand(InteractionHand.MAIN_HAND, stack);
		}
	},
	OFFHAND {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			return entity.getItemInHand(InteractionHand.OFF_HAND).isEmpty();
		}

		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			entity.setItemInHand(InteractionHand.OFF_HAND, stack);
		}
	};
	
	public abstract boolean shouldEquip(LivingEntity entity, ItemStack stack);
	public abstract void equip(LivingEntity entity, ItemStack stack);

	public static PreferredCapSlot fromHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? MAIN_HAND : OFFHAND;
	}
}
