package io.github.ignoramuses.bing_bing_wahoo.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface KeyboardInputExtensions {
	default void wahoo$disableControl() {
		throw new IllegalStateException("Not implemented");
	}

	default void wahoo$enableControl() {
		throw new IllegalStateException("Not implemented");
	}
}
