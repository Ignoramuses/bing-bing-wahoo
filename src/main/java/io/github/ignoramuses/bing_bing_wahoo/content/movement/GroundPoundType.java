package io.github.ignoramuses.bing_bing_wahoo.content.movement;

public enum GroundPoundType {
	DISABLED,
	ENABLED,
	DESTRUCTIVE;
	
	public boolean enabled() {
		return this == ENABLED || this == DESTRUCTIVE;
	}
}
