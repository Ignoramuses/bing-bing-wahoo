package io.github.ignoramuses.bing_bing_wahoo.extensions;

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

	default boolean diving() {
		throw new IllegalStateException("Not implemented");
	}

	default void startDiving() {
		throw new IllegalStateException("Not implemented");
	}

	default void stopAllActions() {
		throw new IllegalStateException("Not implemented");
	}
}
