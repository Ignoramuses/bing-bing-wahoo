package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.ignoramuses.bingBingWahoo.cap.CapPickupType;
import net.ignoramuses.bingBingWahoo.movement.GroundPoundTypes;

@Config(name = "bingbingwahoo")
public class BingBingWahooConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip(count = 2)
	public boolean blj = true;
	@ConfigEntry.Gui.Tooltip(count = 3)
	public boolean rapidFireLongJumps = false;
	@ConfigEntry.Gui.Tooltip(count = 5)
	public GroundPoundTypes groundPoundType = GroundPoundTypes.DESTRUCTIVE;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public double maxLongJumpSpeed = 1.5;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public double longJumpSpeedMultiplier = 10;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public float flipSpeedMultiplier = 1;
	@ConfigEntry.Gui.Tooltip
	public boolean allowNormalWallJumps = false;
	@ConfigEntry.Gui.Tooltip
	public boolean backFlips = true;
	@ConfigEntry.Gui.Tooltip
	public boolean groundedDives = true;
	@ConfigEntry.Gui.Tooltip
	public boolean bonking = true;
	@ConfigEntry.Gui.Tooltip(count = 2)
	public CapPickupType capPickupType = CapPickupType.ALL;
}
