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
	public static boolean forwardsPressed = false;
	public static long millisForwardsHeldFor = 0;
	private static long millisForwardsLastPressedAt = 0;
	
	public static boolean crouchPressed = false;
	public static long millisSinceCrouchPress = 0;
	private static long millisCrouchPressedAt = 0;
	
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(BingBingWahooClient::handleKeyValueUpdates);
	}
	
	public static void handleKeyValueUpdates(MinecraftClient client) {
		// get current value
		boolean forwardsPressed = MinecraftClient.getInstance().options.keyForward.isPressed();
		// if theres a discrepancy, the value was updated this tick
		if (BingBingWahooClient.forwardsPressed != forwardsPressed) {
			// setting new value
			BingBingWahooClient.forwardsPressed = forwardsPressed;
		}
		// if this tick enabled forwards
		if (forwardsPressed) {
			millisForwardsLastPressedAt = System.currentTimeMillis();
		}
		// do not do this if forwards has never been pressed
		if (millisForwardsLastPressedAt != 0) {
			millisForwardsHeldFor = System.currentTimeMillis() - millisForwardsLastPressedAt;
		}
		
		crouchPressed = MinecraftClient.getInstance().options.keySneak.isPressed();
		
	}
}
