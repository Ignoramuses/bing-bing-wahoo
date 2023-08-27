package io.github.ignoramuses.bing_bing_wahoo.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Stable API methods. Unless otherwise noted, these work on client and server.
 */
public class BingBingWahooApi {
	public static boolean isGroundPounding(@Nullable Player player) {
		return false;
	}

	public static boolean isDiving(@Nullable Player player) {
		return false;
	}

	public static boolean isFlyingCap(@Nullable Entity entity) {
		return false;
	}

	/**
	 * Only works on client.
	 */
	public static void startDiving(Player player) {
	}

	/**
	 * Only works on client.
	 */
	public static void stopAllActions(Player player) {
	}
}
