package net.ignoramuses.bingBingWahoo.cap;

import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public enum PreferredCapSlot {
	// priority order
	TRINKETS {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			if (BingBingWahoo.TRINKETS_LOADED) {
				return TrinketsHandler.getCapStack(entity) == null;
			}
			return false;
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			if (BingBingWahoo.TRINKETS_LOADED) {
				TrinketsHandler.equipInHatSlot(entity, stack);
			}
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
