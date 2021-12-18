package net.ignoramuses.bingBingWahoo.cap;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;

public class MysteriousCapArmorMaterial implements ArmorMaterial {
	public static final Ingredient REPAIR = Ingredient.fromTag(ItemTags.WOOL);
	
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
		return REPAIR;
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
