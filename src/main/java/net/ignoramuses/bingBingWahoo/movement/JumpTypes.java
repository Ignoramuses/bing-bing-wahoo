package net.ignoramuses.bingBingWahoo.movement;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public enum JumpTypes {
	NORMAL,
	LONG,
	DOUBLE,
	TRIPLE,
	DIVE,
	WALL,
	BACK_FLIP;
	
	public static JumpTypes fromBuf(FriendlyByteBuf buf) {
		return values()[buf.readByte()];
	}
	
	public boolean isRegularJump() {
		return this == NORMAL || this == DOUBLE || this == TRIPLE || this == LONG;
	}
	
	public boolean canWallJumpFrom() {
		return (isRegularJump() || this == WALL) && this != NORMAL;
	}
	
	public boolean canLongJumpFrom() {
		return isRegularJump() && this != TRIPLE && this != DIVE;
	}
	
	public FriendlyByteBuf toBuf() {
		return new FriendlyByteBuf(PacketByteBufs.create().writeByte(ordinal()));
	}
}
