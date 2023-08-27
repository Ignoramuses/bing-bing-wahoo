package io.github.ignoramuses.bing_bing_wahoo;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BingBingWahoo implements ModInitializer {
	public static final String ID = "bingbingwahoo";
	public static final String NAME = "Bing Bing Wahoo";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	@Override
	public void onInitialize() {
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
