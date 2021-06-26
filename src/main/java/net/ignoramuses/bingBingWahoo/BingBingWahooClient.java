package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static boolean rapidFire = false; // to be implemented
	public static final double MAX_LONG_JUMP_SPEED = 1.5;
	public static final double LONG_JUMP_SPEED_MULTIPLIER = 10;
	
	@Override
	public void onInitializeClient() {
	
	}
	
	public enum JumpTypes {
		NORMAL,
		LONG,
		DOUBLE,
		TRIPLE,
		DIVE,
		WALL
		
		;
		
		public boolean isRegularJump() {
			return this == NORMAL || this == DOUBLE || this == TRIPLE || this == LONG;
		}
		
		public boolean canWallJumpFrom() {
			return (isRegularJump() || this == WALL) && this != NORMAL;
		}
	}
}
