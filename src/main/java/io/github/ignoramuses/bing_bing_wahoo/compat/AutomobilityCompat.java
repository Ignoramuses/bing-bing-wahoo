package io.github.ignoramuses.bing_bing_wahoo.compat;

import io.github.foundationgames.automobility.block.SlopeBlock;
import io.github.foundationgames.automobility.block.SteepSlopeBlock;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AutomobilityCompat {
	public static boolean AUTOMOBILITY_LOADED = FabricLoader.getInstance().isModLoaded("automobility");

	public static boolean isSlope(BlockState state) {
		return isSlope(state.getBlock());
	}

	public static boolean isSlope(Block block) {
		if (!AUTOMOBILITY_LOADED) {
			return false;
		}
		return block instanceof SlopeBlock || block instanceof SteepSlopeBlock;
	}
}
