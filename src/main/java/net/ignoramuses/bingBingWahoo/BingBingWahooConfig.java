package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "bingbingwahoo")
public class BingBingWahooConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip(count = 5)
	public BingBingWahooClient.BLJTypes bljType = BingBingWahooClient.BLJTypes.ENABLED;
	@ConfigEntry.Gui.Tooltip
	public double maxLongJumpSpeed = 1.5;
	@ConfigEntry.Gui.Tooltip
	public double longJumpSpeedMultiplier = 10;
	@ConfigEntry.Gui.Tooltip(count = 3)
	@ConfigEntry.BoundedDiscrete(max = 360, min = 0)
	public int degreesPerFlipFrame = 6;
	@ConfigEntry.Gui.Tooltip
	public boolean allowNormalWallJumps = false;
	@ConfigEntry.Gui.Tooltip
	public boolean backFlips = true;
	@ConfigEntry.Gui.Tooltip
	public boolean groundedDives = true;
}
