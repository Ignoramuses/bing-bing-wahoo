package io.github.ignoramuses.bing_bing_wahoo.content.cap;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class WahooiumMaterial implements ArmorMaterial {
	public static final WahooiumMaterial INSTANCE = new WahooiumMaterial();

	public static final Ingredient REPAIR_INGREDIENT = Ingredient.of(ItemTags.WOOL);
	public static final String NAME = BingBingWahoo.id("wahooium").toString();

	@Override
	public int getDurabilityForType(Type slot) {
		return 128;
	}

	@Override
	public int getDefenseForType(Type slot) {
		return 2;
	}

	@Override
	public int getEnchantmentValue() {
		return 9; // matches iron
	}

	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_LEATHER;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return REPAIR_INGREDIENT;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public float getToughness() {
		return 0; // matches leather
	}

	@Override
	public float getKnockbackResistance() {
		return 0; // none, unused anyway
	}
}
