package net.ignoramuses.bingBingWahoo.cap;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

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
			return entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty();
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			entity.equipStack(EquipmentSlot.HEAD, stack);
		}
	},
	HAND {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			Hand hand = entity.getActiveHand();
			return entity.getStackInHand(hand).isEmpty();
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			Hand hand = entity.getActiveHand();
			entity.setStackInHand(hand, stack);
		}
	},
	@SuppressWarnings("UnstableApiUsage")
	INVENTORY {
		@Override
		public boolean shouldEquip(LivingEntity entity, ItemStack stack) {
			if (entity instanceof PlayerEntity player) {
				return PlayerInventoryStorage.of(player.getInventory()).simulateInsert(ItemVariant.of(stack), stack.getCount(), null) == stack.getCount();
			}
			return false;
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			if (entity instanceof PlayerEntity player) {
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
			return entity.getWorld() instanceof ServerWorld;
		}
		
		@Override
		public void equip(LivingEntity entity, ItemStack stack) {
			if (entity.getWorld() instanceof ServerWorld world) {
				world.spawnEntity(new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), stack));
			} else throw new RuntimeException("called from client");
		}
	};
	
	public abstract boolean shouldEquip(LivingEntity entity, ItemStack stack);
	public abstract void equip(LivingEntity entity, ItemStack stack);
}
