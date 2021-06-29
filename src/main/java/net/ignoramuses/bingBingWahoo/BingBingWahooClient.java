package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static BingBingWahooConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(BingBingWahooConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BingBingWahooConfig.class).getConfig();
	}
}
