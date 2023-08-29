package io.github.ignoramuses.bing_bing_wahoo;

import io.github.ignoramuses.bing_bing_wahoo.compat.trinkets.TrinketsCompat;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.render.CapArmorRenderer;
import io.github.ignoramuses.bing_bing_wahoo.registry.WahooItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class BingBingWahooClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		//noinspection deprecation - not a full replacement
		ArmorRenderer.register(CapArmorRenderer.INSTANCE, WahooItems.MYSTERIOUS_CAP);
		ColorProviderRegistry.ITEM.register(WahooItems.MYSTERIOUS_CAP::getColor, WahooItems.MYSTERIOUS_CAP);

		if (QuiltLoader.isModLoaded("trinkets")) {
			TrinketsCompat.init();
		}
	}
}
