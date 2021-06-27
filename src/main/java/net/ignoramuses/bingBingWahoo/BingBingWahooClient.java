package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static BingBingWahooConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(BingBingWahooConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BingBingWahooConfig.class).getConfig();
	}
	
	public enum JumpTypes {
		NORMAL,
		LONG,
		DOUBLE,
		TRIPLE,
		DIVE,
		WALL,
		BACK_FLIP
		
		;
		
		public boolean isRegularJump() {
			return this == NORMAL || this == DOUBLE || this == TRIPLE || this == LONG;
		}
		
		public boolean canWallJumpFrom() {
			return (isRegularJump() || this == WALL) && this != NORMAL;
		}
	}
	
	public enum BLJTypes {
		ENABLED,
		DISABLED,
		RAPID_FIRE
	}
}
