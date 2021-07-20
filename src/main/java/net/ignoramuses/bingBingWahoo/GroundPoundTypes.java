package net.ignoramuses.bingBingWahoo;

public enum GroundPoundTypes {
	DISABLED,
	ENABLED,
	DESTRUCTIVE;
	
	public boolean enabled() {
		return this == ENABLED || this == DESTRUCTIVE;
	}
}
