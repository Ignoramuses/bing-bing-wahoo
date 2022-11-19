package io.github.ignoramuses.bing_bing_wahoo.extensions;

import io.github.ignoramuses.bing_bing_wahoo.content.movement.JumpType;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerExtensions {
	default void wahoo$setPreviousJumpType(JumpType type) {
		throw new IllegalStateException("Not implemented");
	}

	default void wahoo$setBonked(boolean value) {
		throw new IllegalStateException("Not implemented");
	}

	default void wahoo$setGroundPounding(boolean value, boolean breakBlocks) {
		throw new IllegalStateException("Not implemented");
	}

	default void wahoo$setDiving(boolean value, @Nullable BlockPos startPos) {
		throw new IllegalStateException("Not implemented");
	}

	default void wahoo$setSliding(boolean value) {
		throw new IllegalStateException("Not implemented");
	}

	default void wahoo$setDestructionPermOverride(boolean value) {
		throw new IllegalStateException("Not implemented");
	}
}
