package net.ignoramuses.bingBingWahoo;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerEntityExtensions {
	void setPreviousJumpType(JumpTypes type);
	
	void setGroundPounding(boolean value, boolean breakBlocks);
	
	void setDiving(boolean value, @Nullable BlockPos startPos);
}
