package io.github.ignoramuses.bing_bing_wahoo;

import eu.midnightdust.lib.config.MidnightConfig;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.GroundPoundType;

public class BingBingWahooConfig extends MidnightConfig {
	@Comment(centered = true)
	public static Comment bljComment0, bljComment1;
	@Entry
	public static boolean blj = true;

	@Comment(centered = true)
	public static Comment spacer0, rapidFireLongJumpsComment0, rapidFireLongJumpsComment1, rapidFireLongJumpsComment2;
	@Entry
	public static boolean rapidFireLongJumps = false;

	@Comment(centered = true)
	public static Comment spacer1, allowNormalWallJumpsComment;
	@Entry
	public static boolean allowNormalWallJumps = false;
	
	@Comment(centered = true)
	public static Comment spacer2, backFlipsComment;
	@Entry
	public static boolean backFlips = true;
	
	@Comment(centered = true)
	public static Comment spacer3, groundedDivesComment;
	@Entry
	public static boolean groundedDives = true;

	@Comment(centered = true)
	public static Comment spacer4, bonkingComment;
	@Entry
	public static boolean bonking = true;

	@Comment(centered = true)
	public static Comment spacer5, groundPoundTypeComment0, groundPoundTypeComment1, groundPoundTypeComment2, groundPoundTypeComment3, groundPoundTypeComment4;
	@Entry
	public static GroundPoundType groundPoundType = GroundPoundType.DESTRUCTIVE;

	@Comment(centered = true)
	public static Comment spacer6, maxLongJumpSpeedComment0, maxLongJumpSpeedComment1;
	@Entry // 2
	public static double maxLongJumpSpeed = 1.5;

	@Comment(centered = true)
	public static Comment spacer7, longJumpSpeedMultiplierComment0, longJumpSpeedMultiplierComment1;
	@Entry // 2
	public static double longJumpSpeedMultiplier = 10;

	@Comment(centered = true)
	public static Comment spacer8, flipSpeedMultiplierComment0, flipSpeedMultiplierComment1;
	@Entry // 2
	public static float flipSpeedMultiplier = 1;
}
