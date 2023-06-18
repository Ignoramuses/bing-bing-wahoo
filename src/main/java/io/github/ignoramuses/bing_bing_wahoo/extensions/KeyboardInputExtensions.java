package io.github.ignoramuses.bing_bing_wahoo.extensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface KeyboardInputExtensions {
	default void disableControl() {
		throw new IllegalStateException("Not implemented");
	}

	default void enableControl() {
		throw new IllegalStateException("Not implemented");
	}
}
