package net.ignoramuses.bing_bing_wahoo.extensions;

import net.ignoramuses.bing_bing_wahoo.movement.JumpTypes;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerExtensions {
	void wahoo$setPreviousJumpType(JumpTypes type);
	void wahoo$setBonked(boolean value);
	void wahoo$setGroundPounding(boolean value, boolean breakBlocks);
	void wahoo$setDiving(boolean value, @Nullable BlockPos startPos);
	void wahoo$setSliding(boolean value);
	void wahoo$setDestructionPermOverride(boolean value);
}