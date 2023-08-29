package io.github.ignoramuses.bing_bing_wahoo.content.cap;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;

public class CapItem extends DyeableArmorItem {
	public static final int WHITE = 0xFFFFFF;

	public CapItem(ArmorMaterial material, Properties properties) {
		super(material, Type.HELMET, properties);
	}

	public CapItem(Properties settings) {
		this(WahooiumMaterial.INSTANCE, settings);
	}

	@Override
	public int getColor(ItemStack stack) {
		int color = super.getColor(stack);
		return color == DEFAULT_LEATHER_COLOR ? WHITE : color;
	}

	public int getColor(ItemStack stack, int tintIndex) {
		return tintIndex == 0 ? getColor(stack) : -1;
	}
}
