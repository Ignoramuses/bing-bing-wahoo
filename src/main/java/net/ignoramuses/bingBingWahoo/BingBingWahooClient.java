package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static boolean rapidFire = true;
	
	@Override
	public void onInitializeClient() {
	
	}
	
	public enum JumpTypes {
		LONG,
		DOUBLE,
		TRIPLE
	}
}
