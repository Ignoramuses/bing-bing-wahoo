package net.ignoramuses.bing_bing_wahoo.movement;

public enum GroundPoundTypes {
	DISABLED,
	ENABLED,
	DESTRUCTIVE;
	
	public boolean enabled() {
		return this == ENABLED || this == DESTRUCTIVE;
	}
}
