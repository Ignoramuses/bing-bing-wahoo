package io.github.ignoramuses.bing_bing_wahoo.extensions;

public interface PlayerExtensions {
	default boolean wahoo$getSliding() {
		throw new IllegalStateException("Not implemented");
	}
}
