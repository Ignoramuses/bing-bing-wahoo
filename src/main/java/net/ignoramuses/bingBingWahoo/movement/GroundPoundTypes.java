package net.ignoramuses.bingBingWahoo.movement;

public enum GroundPoundTypes {
	DISABLED,
	ENABLED,
	DESTRUCTIVE;
	
	public boolean enabled() {
		return this == ENABLED || this == DESTRUCTIVE;
	}
}
