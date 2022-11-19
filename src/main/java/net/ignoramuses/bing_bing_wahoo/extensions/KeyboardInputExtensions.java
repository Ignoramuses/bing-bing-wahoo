package net.ignoramuses.bing_bing_wahoo.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface KeyboardInputExtensions {
	void wahoo$disableControl();
	void wahoo$enableControl();
}
