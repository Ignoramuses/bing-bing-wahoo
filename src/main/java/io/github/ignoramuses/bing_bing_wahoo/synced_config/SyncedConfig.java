package io.github.ignoramuses.bing_bing_wahoo.synced_config;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahooConfig;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.GroundPoundType;
import io.github.ignoramuses.bing_bing_wahoo.packets.UpdateSyncedBooleanPacket;
import io.github.ignoramuses.bing_bing_wahoo.packets.UpdateSyncedDoublePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.Value;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * SyncedConfigs are client-side configurable values that can be overriden by GameRules.
 * @param <T> the configurable type
 * @param <V> the GameRule Value type
 */
public class SyncedConfig<T, V extends Value<V>> {
	public static final Map<String, SyncedConfig<Boolean, BooleanValue>> BOOLEAN_CONFIGS = new HashMap<>();
	public static final Map<String, SyncedConfig<Double, DoubleRule>> DOUBLE_CONFIGS = new HashMap<>();

	public static final SyncedConfig<Boolean, BooleanValue>
			DESTRUCTIVE_GROUND_POUNDS = registerBoolean("destructiveGroundPounds", true, () -> BingBingWahooConfig.groundPoundType == GroundPoundType.DESTRUCTIVE),
			BACKWARDS_LONG_JUMPS = registerBoolean("backwardsLongJumps", true, () -> BingBingWahooConfig.blj),
			RAPID_FIRE_LONG_JUMPS = registerBoolean("rapidFireLongJumps", true, () -> BingBingWahooConfig.rapidFireLongJumps),
			CAP_REQUIRED = registerBoolean("mysteriousCapRequired", true, () -> true);

	public static final SyncedConfig<Double, DoubleRule>
			MAX_LONG_JUMP_SPEED = registerDouble("longJumpMaxSpeed", 1.5, 0, () -> BingBingWahooConfig.maxLongJumpSpeed),
			LONG_JUMP_SPEED_MULTIPLIER = registerDouble("longJumpSpeedMultiplier", 10, 0, () -> BingBingWahooConfig.longJumpSpeedMultiplier);

	public final GameRules.Key<V> ruleKey;
	public final String name;

	private final Supplier<T> configValue;
	private final T defaultRuleValue;
	@Internal
	public T currentRuleValue;

	public SyncedConfig(String name, T defaultRuleValue, Supplier<T> configValue, Category category, GameRules.Type<V> type) {
		this.name = name;
		this.defaultRuleValue = defaultRuleValue;
		this.currentRuleValue = defaultRuleValue;
		this.configValue = configValue;
		this.ruleKey = GameRuleRegistry.register(name, category, type);
	}

	public SyncedConfig(String name, T defaultRuleValue, Supplier<T> configValue, GameRules.Type<V> type) {
		this(name, defaultRuleValue, configValue, Category.PLAYER, type);
	}

	public static SyncedConfig<Boolean, BooleanValue> registerBoolean(String name, boolean defaultRuleValue, Supplier<Boolean> configValue) {
		SyncedConfig<Boolean, BooleanValue> config = new SyncedConfig<>(
				name, defaultRuleValue, configValue,
				GameRuleFactory.createBooleanRule(defaultRuleValue, new SyncedBooleanUpdater(name))
		);
		BOOLEAN_CONFIGS.put(name, config);
		return config;
	}

	public static SyncedConfig<Double, DoubleRule> registerDouble(String name, double defaultRuleValue, double min, Supplier<Double> configValue) {
		SyncedConfig<Double, DoubleRule> config = new SyncedConfig<>(
				name, defaultRuleValue, configValue,
				GameRuleFactory.createDoubleRule(defaultRuleValue, min, Double.MAX_VALUE, new SyncedDoubleUpdater(name))
		);
		DOUBLE_CONFIGS.put(name, config);
		return config;
	}

	/**
	 * May only be used on the client.
	 * For server-side getting, use {@link SyncedConfig#ruleKey} on a {@link GameRules} instance.
	 */
	@Environment(EnvType.CLIENT)
	public T get() {
		// if rule is default, let client choose
		if (defaultRuleValue == currentRuleValue) {
			return configValue.get();
		} else { // otherwise force override
			return currentRuleValue;
		}
	}

	public static void init() {
		// sync values to players on join
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			GameRules rules = server.getGameRules();
			for (SyncedConfig<Boolean, BooleanValue> config : BOOLEAN_CONFIGS.values()) {
				UpdateSyncedBooleanPacket.send(sender, config, rules);
			}
			for (SyncedConfig<Double, DoubleRule> config : DOUBLE_CONFIGS.values()) {
				UpdateSyncedDoublePacket.send(sender, config, rules);
			}
		});
	}
}
