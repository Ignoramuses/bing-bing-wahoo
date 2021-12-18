package net.ignoramuses.bingBingWahoo.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.ignoramuses.bingBingWahoo.BingBingWahooConfig;

public class BingBingWahooIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfig.getConfigScreen(BingBingWahooConfig.class, parent).get();
	}
}
