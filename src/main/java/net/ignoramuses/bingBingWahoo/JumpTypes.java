package net.ignoramuses.bingBingWahoo;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public enum JumpTypes {
	NORMAL,
	LONG,
	DOUBLE,
	TRIPLE,
	DIVE,
	WALL,
	BACK_FLIP;
	
	public static JumpTypes fromBuf(PacketByteBuf buf) {
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
	
	public PacketByteBuf toBuf() {
		return new PacketByteBuf(PacketByteBufs.create().writeByte(ordinal()));
	}
}
