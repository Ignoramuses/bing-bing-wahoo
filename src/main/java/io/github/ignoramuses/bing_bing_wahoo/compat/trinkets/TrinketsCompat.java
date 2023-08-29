package io.github.ignoramuses.bing_bing_wahoo.compat.trinkets;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import io.github.ignoramuses.bing_bing_wahoo.registry.WahooItems;

public class TrinketsCompat {
	public static void init() {
		TrinketRendererRegistry.registerRenderer(WahooItems.MYSTERIOUS_CAP, CapTrinketRenderer.INSTANCE);
	}
}
