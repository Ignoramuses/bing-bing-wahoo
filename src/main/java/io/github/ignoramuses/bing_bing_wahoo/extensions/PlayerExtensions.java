package io.github.ignoramuses.bing_bing_wahoo.extensions;

public interface PlayerExtensions {
	default boolean getSliding() {
		throw new IllegalStateException("Not implemented");
	}
}
