package net.ignoramuses.bing_bing_wahoo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.ignoramuses.bing_bing_wahoo.cap.FlyingCapEntity;
import net.ignoramuses.bing_bing_wahoo.cap.MysteriousCapArmorMaterial;
import net.ignoramuses.bing_bing_wahoo.cap.MysteriousCapItem;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BingBingWahoo implements ModInitializer {
	public static final String ID = "bingbingwahoo";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final TagKey<Block> SLIDES = TagKey.create(Registry.BLOCK_REGISTRY, id("slides"));

	@Override
	public void onInitialize() {
		WahooNetworking.init();
		WahooCommands.init();
		WahooRegistry.init();
	}
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
