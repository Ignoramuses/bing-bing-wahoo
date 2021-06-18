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
	public static long tickNum = 0;
	public static long ticksSneakPressedFor = 0;
	public static boolean sneakPressed = false;
	
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(BingBingWahooClient::handleKeyUpdates);
	}
	
	public static void handleKeyUpdates(MinecraftClient client) {
		// discrepancy, value was updated this tick
		if (sneakPressed != MinecraftClient.getInstance().options.keySneak.isPressed()) {
			sneakPressed = MinecraftClient.getInstance().options.keySneak.isPressed();
			
			// value was set to true
			if (sneakPressed) {
			} else {
				// value was set to false
			}
		} else {
			// value is the same as last tick
			if (sneakPressed) {
				ticksSneakPressedFor++;
			} else {
				ticksSneakPressedFor = 0;
			}
		}
		
		tickNum++;
	}
}
