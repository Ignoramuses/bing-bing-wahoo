package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "bingbingwahoo")
public class BingBingWahooConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip(count = 5)
	public BLJTypes bljType = BLJTypes.ENABLED;
	@ConfigEntry.Gui.Tooltip(count = 4)
	public GroundPoundTypes groundPoundType = GroundPoundTypes.DESTRUCTIVE;
	@ConfigEntry.Gui.Tooltip
	public double maxLongJumpSpeed = 1.5;
	@ConfigEntry.Gui.Tooltip
	public double longJumpSpeedMultiplier = 10;
	@ConfigEntry.Gui.Tooltip
	public float flipSpeedMultiplier = 1;
	@ConfigEntry.Gui.Tooltip
	public boolean allowNormalWallJumps = false;
	@ConfigEntry.Gui.Tooltip
	public boolean backFlips = true;
	@ConfigEntry.Gui.Tooltip
	public boolean groundedDives = true;
}
