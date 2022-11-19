package io.github.ignoramuses.bing_bing_wahoo;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import io.github.ignoramuses.bing_bing_wahoo.content.SliderRecordItem;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapEntity;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapArmorMaterial;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapItem;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;

public class WahooRegistry {

	// sound

	public static final SoundEvent SLIDER_SOUND = Registry.register(
			Registry.SOUND_EVENT,
			SliderRecordItem.SOUND_ID,
			new SoundEvent(SliderRecordItem.SOUND_ID)
	);

	// items

	public static final DyeableArmorItem MYSTERIOUS_CAP = Registry.register(
			Registry.ITEM,
			BingBingWahoo.id("mysterious_cap"),
			new MysteriousCapItem(
					MysteriousCapArmorMaterial.INSTANCE,
					EquipmentSlot.HEAD,
					new FabricItemSettings()
							.rarity(Rarity.RARE)
							.durability(128)
							.tab(CreativeModeTab.TAB_MISC)
			)
	);

	public static final RecordItem MUSIC_DISC_SLIDER = Registry.register(
			Registry.ITEM,
			BingBingWahoo.id("music_disc_slider"),
			new SliderRecordItem(
					14,
					SLIDER_SOUND,
					new FabricItemSettings()
							.rarity(Rarity.RARE)
							.maxCount(1)
							.group(CreativeModeTab.TAB_MISC)
			)
	);

	// entity

	public static final EntityType<FlyingCapEntity> FLYING_CAP = Registry.register(
			Registry.ENTITY_TYPE,
			BingBingWahoo.id("flying_cap"),
			FabricEntityTypeBuilder.<FlyingCapEntity>create(MobCategory.MISC, FlyingCapEntity::new)
					.dimensions(EntityDimensions.fixed(0.75f, 0.5f))
					.fireImmune()
					.disableSummon()
					.build()
	);

	public static void init() {
	}
}
