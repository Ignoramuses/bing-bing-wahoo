package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static boolean rapidFire = true;
	public static final double MAX_LONG_JUMP_SPEED = 2;
	public static final double LONG_JUMP_SPEED_MULTIPLIER = 10;
	public static final Direction[] CARDINAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	
	@Override
	public void onInitializeClient() {
	
	}
	
	public enum JumpTypes {
		NORMAL,
		LONG,
		DOUBLE,
		TRIPLE,
		DIVE
	}
}
