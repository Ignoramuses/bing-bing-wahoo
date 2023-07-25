package io.github.ignoramuses.bing_bing_wahoo.content.movement;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.compat.TemplatesCompat;
import io.github.ignoramuses.bing_bing_wahoo.synced_config.SyncedConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public class Slopes {
	public static final TagKey<Block> STANDARD_SLOPES = TagKey.create(Registries.BLOCK, BingBingWahoo.id("standard_slopes"));
	public static final TagKey<Block> BACKWARDS_SLOPES = TagKey.create(Registries.BLOCK, BingBingWahoo.id("backwards_slopes"));

	private static final List<Block> invalid = new CopyOnWriteArrayList<>();

	@Nullable
	public static Direction getSlideDirection(BlockState slope) {
		Block block = slope.getBlock();
		if (invalid.contains(block)) {
			return null; // checked previously, no facing property
		} else if (slope.is(STANDARD_SLOPES)) {
			return getSlideDirectionInternal(slope, "standard_slopes");
		} else if (slope.is(BACKWARDS_SLOPES)) {
			Direction direction = getSlideDirectionInternal(slope, "backwards_slopes");
			if (direction != null)
				return direction.getOpposite();
		} else if (TemplatesCompat.IS_LOADED) {
			return TemplatesCompat.getSlideDirection(slope);
		}

		return null;
	}

	public static boolean isSlope(BlockState state) {
		return getSlideDirection(state) != null;
	}

	@Nullable
	private static Direction getSlideDirectionInternal(BlockState slope, String tagName) {
		if (slope.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			if (slope.hasProperty(BlockStateProperties.HALF)) {
				Half half = slope.getValue(BlockStateProperties.HALF);
				if (half == Half.TOP && !SyncedConfig.CONVEYOR_GLITCH.get()) {
					return null; // top half, conveyor glitch disabled. no sliding.
				}
			}
			return slope.getValue(BlockStateProperties.HORIZONTAL_FACING);
		} else {
			Block block = slope.getBlock();
			ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
			BingBingWahoo.LOGGER.error("Invalid block in " + tagName + " tag; does not have the horizontal facing property: " + id);
			invalid.add(block);
			return null;
		}
	}
}
