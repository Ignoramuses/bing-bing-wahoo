package io.github.ignoramuses.bing_bing_wahoo.extensions;

import io.github.ignoramuses.bing_bing_wahoo.content.movement.JumpType;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerExtensions {
	default void setPreviousJumpType(JumpType type) {
		throw new IllegalStateException("Not implemented");
	}

	default void setBonked(boolean value) {
		throw new IllegalStateException("Not implemented");
	}

	default void setGroundPounding(boolean value, boolean breakBlocks) {
		throw new IllegalStateException("Not implemented");
	}

	default void setDiving(boolean value, @Nullable BlockPos startPos) {
		throw new IllegalStateException("Not implemented");
	}

	default void setSliding(boolean value) {
		throw new IllegalStateException("Not implemented");
	}

	default void setDestructionPermOverride(boolean value) {
		throw new IllegalStateException("Not implemented");
	}
}
