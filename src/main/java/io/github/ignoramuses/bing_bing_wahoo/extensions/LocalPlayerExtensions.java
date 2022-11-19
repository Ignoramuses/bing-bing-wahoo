package io.github.ignoramuses.bing_bing_wahoo.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface LocalPlayerExtensions {
	default boolean wahoo$groundPounding() {
		throw new IllegalStateException("Not implemented");
	}

	default boolean wahoo$slidingOnSlope() {
		throw new IllegalStateException("Not implemented");
	}

	default boolean wahoo$slidingOnGround() {
		throw new IllegalStateException("Not implemented");
	}
}
