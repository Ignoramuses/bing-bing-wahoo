package io.github.ignoramuses.bing_bing_wahoo;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BingBingWahoo implements ModInitializer {
	public static final String ID = "bingbingwahoo";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final TagKey<Block> SLIDES = TagKey.create(Registries.BLOCK, id("slides"));

	@Override
	public void onInitialize() {
		WahooNetworking.init();
		WahooCommands.init();
		WahooRegistry.init();
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
