package io.github.ignoramuses.bing_bing_wahoo.compat;

import io.github.cottonmc.templates.block.TemplateSlopeBlock;
import io.github.cottonmc.templates.util.Edge;
import io.github.ignoramuses.bing_bing_wahoo.synced_config.SyncedConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TemplatesCompat {
	private static final Set<Object> down = tryMake(() -> Set.of(
			Edge.DOWN_EAST, Edge.DOWN_WEST, Edge.DOWN_NORTH, Edge.DOWN_SOUTH
	));
	private static final Set<Object> up = tryMake(() -> Set.of(
			Edge.UP_EAST, Edge.UP_WEST, Edge.UP_NORTH, Edge.UP_SOUTH
	));
	// directions inverted, facing is opposite of push direction
	private static final Map<Object, Direction> edgeToFacing = tryMake(() -> Map.of(
			Edge.DOWN_EAST, Direction.WEST,
			Edge.DOWN_WEST, Direction.EAST,
			Edge.DOWN_NORTH, Direction.SOUTH,
			Edge.DOWN_SOUTH, Direction.NORTH,

			Edge.UP_EAST, Direction.WEST,
			Edge.UP_WEST, Direction.EAST,
			Edge.UP_NORTH, Direction.SOUTH,
			Edge.UP_SOUTH, Direction.NORTH
	));

	// true when all necessary classes and fields are present
	public static final boolean IS_LOADED = down != null && up != null && edgeToFacing != null && tryMake(() -> TemplateSlopeBlock.EDGE) != null;

	public static Direction getSlideDirection(BlockState slope) {
		if (slope.hasProperty(TemplateSlopeBlock.EDGE) && canSlideOnSlope(slope)) {
			Edge edge = slope.getValue(TemplateSlopeBlock.EDGE);
			return edgeToFacing.get(edge);
		}
		return null;
	}

	private static boolean canSlideOnSlope(BlockState slope) {
		Edge edge = slope.getValue(TemplateSlopeBlock.EDGE);
		if (down.contains(edge)) {
			return true; // bottom half slopes always work
		} else if (SyncedConfig.CONVEYOR_GLITCH.get()) {
			return up.contains(edge); // only use top half slopes when conveyor glitch is enabled
		}
		return false; // some non-slope edge
	}

	private static <T> T tryMake(Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Throwable t) {
			return null;
		}
	}
}
