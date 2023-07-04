package io.github.ignoramuses.bing_bing_wahoo.api;

import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapEntity;
import io.github.ignoramuses.bing_bing_wahoo.extensions.LocalPlayerExtensions;
import io.github.ignoramuses.bing_bing_wahoo.extensions.ServerPlayerExtensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Stable API methods. Unless otherwise noted, these work on client and server.
 */
public class BingBingWahooApi {
	public static boolean isGroundPounding(@Nullable Player player) {
		if (player instanceof LocalPlayerExtensions ex)
			return ex.groundPounding();
		else if (player instanceof ServerPlayerExtensions ex)
			return ex.isGroundPounding();
		return false;
	}

	public static boolean isDiving(@Nullable Player player) {
		if (player instanceof LocalPlayerExtensions ex)
			return ex.diving();
		if (player instanceof ServerPlayerExtensions ex)
			return ex.isDiving();
		return false;
	}

	public static boolean isFlyingCap(@Nullable Entity entity) {
		return entity instanceof FlyingCapEntity;
	}

	/**
	 * Only works on client.
	 */
	public static void startDiving(Player player) {
		if (player instanceof LocalPlayerExtensions ex)
			ex.startDiving();
	}

	/**
	 * Only works on client.
	 */
	public static void stopAllActions(Player player) {
		if (player instanceof LocalPlayerExtensions ex)
			ex.stopAllActions();
	}
}
