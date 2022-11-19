package net.ignoramuses.bing_bing_wahoo.cap;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum MysteriousCapArmorMaterial implements ArmorMaterial {
	INSTANCE;
	
	public static final Ingredient REPAIR = Ingredient.of(ItemTags.WOOL);
	
	@Override
	public int getDurabilityForSlot(EquipmentSlot slot) {
		return 128;
	}
	
	@Override
	public int getDefenseForSlot(EquipmentSlot slot) {
		return 1;
	}
	
	@Override
	public int getEnchantmentValue() {
		return 15;
	}
	
	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_LEATHER;
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
