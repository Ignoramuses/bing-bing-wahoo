package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static boolean rapidFire = true;
	public static final double MAX_LONG_JUMP_SPEED = 2;
	public static final double LONG_JUMP_SPEED_MULTIPLIER = 5;
	
	@Override
	public void onInitializeClient() {
	
	}
	
	public enum JumpTypes {
		NORMAL,
		LONG,
		DOUBLE,
		TRIPLE
	}
}
