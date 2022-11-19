package io.github.ignoramuses.bing_bing_wahoo.extensions;

import io.github.ignoramuses.bing_bing_wahoo.content.movement.FlipState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface AbstractClientPlayerExtensions {
	default int wahoo$ticksFlipping() {
		throw new IllegalStateException("Not implemented");
	}

	default void wahoo$setFlipState(FlipState state) {
		throw new IllegalStateException("Not implemented");
	}

	default boolean wahoo$flippingForwards() {
		throw new IllegalStateException("Not implemented");
	}
}
