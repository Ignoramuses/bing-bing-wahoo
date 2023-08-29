package io.github.ignoramuses.bing_bing_wahoo.registry;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.CapItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class WahooItems {
	public static final CapItem MYSTERIOUS_CAP = register("mysterious_cap", new CapItem(new FabricItemSettings()));

	private static <T extends Item> T register(String name, T item) {
		return Registry.register(BuiltInRegistries.ITEM, BingBingWahoo.id(name), item);
	}

	public static void init() {
	}
}
