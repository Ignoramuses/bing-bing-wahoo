package io.github.ignoramuses.bing_bing_wahoo;

import io.github.ignoramuses.bing_bing_wahoo.registry.WahooItems;
import net.minecraft.resources.ResourceLocation;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BingBingWahoo implements ModInitializer {
	public static final String ID = "bingbingwahoo";
	public static final String NAME = "Bing Bing Wahoo";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	@Override
	public void onInitialize(ModContainer mod) {
		WahooItems.init();
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
