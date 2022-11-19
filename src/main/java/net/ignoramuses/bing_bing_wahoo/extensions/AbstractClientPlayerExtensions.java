package net.ignoramuses.bing_bing_wahoo.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface AbstractClientPlayerExtensions {
	int wahoo$ticksFlipping();
	void wahoo$setFlipping(boolean value);
	void wahoo$setFlipDirection(boolean forwards);
	boolean wahoo$flippingForwards();
}
