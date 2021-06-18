package net.ignoramuses.bingBingWahoo.player;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class YeetProcessing {
	
	public static boolean shouldYeet(PlayerEntity player) {
		if (BingBingWahooClient.ticksSneakPressedFor < 4 && BingBingWahooClient.ticksSneakPressedFor > 0) {
			return true;
		}
		
		return false;
	}
	
	public static void yeetPlayer(PlayerEntity player) {
		if (shouldYeet(player)) {
			player.addVelocity(0f, 1f, 0f);
		}
	}
}
