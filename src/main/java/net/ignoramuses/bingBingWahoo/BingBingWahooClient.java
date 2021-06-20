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
	public static final Vec3d MAX_LONG_JUMP_SPEED = new Vec3d(2, 1, 2);
	
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
