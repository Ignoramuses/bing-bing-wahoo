package io.github.ignoramuses.bing_bing_wahoo.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface LocalPlayerExtensions {
	default boolean groundPounding() {
		throw new IllegalStateException("Not implemented");
	}

	default boolean slidingOnSlope() {
		throw new IllegalStateException("Not implemented");
	}

	default boolean slidingOnGround() {
		throw new IllegalStateException("Not implemented");
	}
}
