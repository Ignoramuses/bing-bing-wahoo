package net.ignoramuses.bingBingWahoo;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class MysteriousCapArmorMaterial implements ArmorMaterial {
	@Override
	public int getDurability(EquipmentSlot slot) {
		return 128;
	}
	
	@Override
	public int getProtectionAmount(EquipmentSlot slot) {
		return 1;
	}
	
	@Override
	public int getEnchantability() {
		return 15;
	}
	
	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
	}
	
	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.ofItems(Items.BLACK_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.CYAN_WOOL, Items.GRAY_WOOL, Items.GREEN_WOOL,
				Items.LIGHT_BLUE_WOOL, Items.LIGHT_GRAY_WOOL, Items.LIME_WOOL, Items.MAGENTA_WOOL, Items.ORANGE_WOOL, Items.PINK_WOOL,
				Items.PURPLE_WOOL, Items.RED_WOOL, Items.WHITE_WOOL, Items.YELLOW_WOOL);
	}
	
	@Override
	public String getName() {
		return "mysterious_cap";
	}
	
	@Override
	public float getToughness() {
		return 0;
	}
	
	@Override
	public float getKnockbackResistance() {
		return 0;
	}
}
