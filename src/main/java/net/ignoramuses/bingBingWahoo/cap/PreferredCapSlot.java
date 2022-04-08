package net.ignoramuses.bingBingWahoo.cap;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
	HAND {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			InteractionHand hand = entity.getUsedItemHand();
			return entity.getItemInHand(hand).isEmpty();
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			InteractionHand hand = entity.getUsedItemHand();
			entity.setItemInHand(hand, stack);
		}
	},
	@SuppressWarnings("UnstableApiUsage")
	INVENTORY {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			if (entity instanceof Player player) {
				return PlayerInventoryStorage.of(player.getInventory()).simulateInsert(ItemVariant.of(stack), stack.getCount(), null) == stack.getCount();
			}
			return false;
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			if (entity instanceof Player player) {
				try (Transaction t = Transaction.openOuter()) {
					PlayerInventoryStorage.of(player.getInventory()).insert(ItemVariant.of(stack), stack.getCount(), t);
					t.commit();
				}
			} else throw new RuntimeException("INVENTORY slot cannot be used for non-player entities");
		}
	},
	WORLD {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			return entity.getLevel() instanceof ServerLevel;
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			if (entity.getLevel() instanceof ServerLevel world) {
				world.addFreshEntity(new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), stack));
			} else throw new RuntimeException("called from client");
		}
	};
	
	public abstract boolean shouldEquip(LivingEntity entity, ItemStack stack);
	public abstract void equip(LivingEntity entity, ItemStack stack);
}
