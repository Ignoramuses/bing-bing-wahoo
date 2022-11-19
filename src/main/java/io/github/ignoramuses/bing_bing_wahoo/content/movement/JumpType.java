package io.github.ignoramuses.bing_bing_wahoo.content.movement;

public enum JumpType {
	NORMAL,
	LONG,
	DOUBLE,
	TRIPLE,
	DIVE,
	WALL,
	BACK_FLIP;

	public boolean isRegularJump() {
		return this == NORMAL || this == DOUBLE || this == TRIPLE || this == LONG;
	}
	
	public boolean canWallJumpFrom() {
		return (isRegularJump() || this == WALL) && this != NORMAL;
	}
	
	public boolean canLongJumpFrom() {
		return isRegularJump() && this != TRIPLE && this != DIVE;
	}
}
