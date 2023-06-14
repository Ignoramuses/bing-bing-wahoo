package io.github.ignoramuses.bing_bing_wahoo.content.cap;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public enum MysteriousCapArmorMaterial implements ArmorMaterial {
	INSTANCE;
	
	public static final Ingredient REPAIR = Ingredient.of(ItemTags.WOOL);

	@Override
	public int getDurabilityForType(Type slot) {
		return 128;
	}

	@Override
	public int getDefenseForType(Type slot) {
		return 1;
	}

	@Override
	public int getEnchantmentValue() {
		return 15;
	}
	
	@Override
	@NotNull
	public SoundEvent getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_LEATHER;
	}
	
	@Override
	@NotNull
	public Ingredient getRepairIngredient() {
		return REPAIR;
	}
	
	@Override
	@NotNull
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
