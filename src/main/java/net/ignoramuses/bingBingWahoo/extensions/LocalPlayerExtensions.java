package net.ignoramuses.bingBingWahoo.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface LocalPlayerExtensions {
	boolean wahoo$groundPounding();
	boolean wahoo$slidingOnSlope();
	boolean wahoo$slidingOnGround();
}
