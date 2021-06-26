package net.ignoramuses.bingBingWahoo;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.serialization.ConfigSerializer;
import dev.inkwell.conrad.api.value.serialization.FlatOwenSerializer;
import dev.inkwell.owen.OwenElement;
import org.jetbrains.annotations.NotNull;

public class BingBingWahooConfig extends Config<OwenElement> {
	@Override
	public @NotNull ConfigSerializer<OwenElement> getSerializer() {
		return FlatOwenSerializer.INSTANCE;
	}
	
	@Override
	public @NotNull SaveType getSaveType() {
		return SaveType.ROOT;
	}
	
	public static final ValueKey<Integer> DEGREES_PER_FLIP_FRAME = value(6);
	public static final ValueKey<Integer> LONG_JUMP_SPEED_MULTIPLIER = value(10);
	public static final ValueKey<Double> MAX_LONG_JUMP_SPEED = value(1.5);
	public static final ValueKey<Boolean> RAPID_FIRE = value(false);
}
