package io.github.ignoramuses.bing_bing_wahoo;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import io.github.ignoramuses.bing_bing_wahoo.content.SliderRecordItem;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapEntity;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapArmorMaterial;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;

public class WahooRegistry {

	// sound

	public static final SoundEvent SLIDER_SOUND = Registry.register(
			BuiltInRegistries.SOUND_EVENT,
			SliderRecordItem.SOUND_ID,
			SoundEvent.createVariableRangeEvent(SliderRecordItem.SOUND_ID)
	);

	// items

	public static final DyeableArmorItem MYSTERIOUS_CAP = Registry.register(
			BuiltInRegistries.ITEM,
			BingBingWahoo.id("mysterious_cap"),
			new MysteriousCapItem(
					MysteriousCapArmorMaterial.INSTANCE,
					EquipmentSlot.HEAD,
					new FabricItemSettings()
							.rarity(Rarity.RARE)
							.durability(128)
			)
	);

	public static final RecordItem MUSIC_DISC_SLIDER = Registry.register(
			BuiltInRegistries.ITEM,
			BingBingWahoo.id("music_disc_slider"),
			new SliderRecordItem(
					14,
					SLIDER_SOUND,
					new FabricItemSettings()
							.rarity(Rarity.RARE)
							.maxCount(1)
			)
	);

	// entity

	public static final EntityType<FlyingCapEntity> FLYING_CAP = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			BingBingWahoo.id("flying_cap"),
			FabricEntityTypeBuilder.<FlyingCapEntity>create(MobCategory.MISC, FlyingCapEntity::new)
					.dimensions(EntityDimensions.fixed(0.75f, 0.5f))
					.fireImmune()
					.disableSummon()
					.build()
	);

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> entries.addAfter(Items.TURTLE_HELMET, MYSTERIOUS_CAP));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(MUSIC_DISC_SLIDER));
	}
}
