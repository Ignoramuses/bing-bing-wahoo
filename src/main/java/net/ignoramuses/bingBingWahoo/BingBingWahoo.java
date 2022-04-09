package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.ignoramuses.bingBingWahoo.cap.CapPickupType;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapArmorMaterial;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapItem;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BingBingWahoo implements ModInitializer {
	public static final String ID = "bingbingwahoo";
	public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
	public static final Map<String, CapPickupType> PLAYERS_TO_TYPES = new HashMap<>();
	public static final TagKey<Block> SLIDES = TagKey.create(Registry.BLOCK_REGISTRY, id("slides"));
	public static DyeableArmorItem MYSTERIOUS_CAP;
	public static SoundEvent SLIDER_SOUND;
	public static RecordItem MUSIC_DISC_SLIDER;
	public static EntityType<FlyingCapEntity> FLYING_CAP;
	
	@Override
	public void onInitialize() {
		WahooNetworking.init();
		WahooCommands.init();

		MYSTERIOUS_CAP = Registry.register(Registry.ITEM, BingBingWahoo.id("mysterious_cap"),
				new MysteriousCapItem(MysteriousCapArmorMaterial.INSTANCE, EquipmentSlot.HEAD,
						new FabricItemSettings().rarity(Rarity.RARE).durability(128).tab(CreativeModeTab.TAB_MISC)));

		ResourceLocation sliderId = id("music_disc_slider");
		SLIDER_SOUND = Registry.register(Registry.SOUND_EVENT, sliderId, new SoundEvent(sliderId));

		MUSIC_DISC_SLIDER = Registry.register(Registry.ITEM, id("music_disc_slider"),
				new SliderRecordItem(14, SLIDER_SOUND,
						new FabricItemSettings().maxCount(1).group(CreativeModeTab.TAB_MISC).rarity(Rarity.RARE)));

		FLYING_CAP = Registry.register(Registry.ENTITY_TYPE, BingBingWahoo.id("flying_cap"),
				FabricEntityTypeBuilder.<FlyingCapEntity>create(MobCategory.MISC, FlyingCapEntity::new)
						.dimensions(EntityDimensions.fixed(0.75f, 0.5f))
						.fireImmune()
						.disableSummon()
						.build());
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
