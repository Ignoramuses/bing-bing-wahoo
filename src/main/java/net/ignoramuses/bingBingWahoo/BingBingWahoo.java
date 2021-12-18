package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapArmorMaterial;
import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class BingBingWahoo implements ModInitializer {
	public static final Direction[] CARDINAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	public static final String ID = "bingbingwahoo";
	public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
	public static final Tag<Block> SLIDES = TagFactory.BLOCK.create(new Identifier(ID, "slides"));
	public static final ArmorMaterial MYSTERIOUS_CAP_MATERIAL = new MysteriousCapArmorMaterial();
	public static DyeableArmorItem MYSTERIOUS_CAP;
	public static Item MUSIC_DISC_SLIDER;
	public static final Identifier SLIDER_ID = new Identifier(ID, "music_disc_slider");
	public static SoundEvent SLIDER;
	
	@Override
	public void onInitialize() {
		WahooNetworking.init();
		WahooCommands.init();
		MYSTERIOUS_CAP = Registry.register(Registry.ITEM, new Identifier(ID, "mysterious_cap"),
				new DyeableArmorItem(MYSTERIOUS_CAP_MATERIAL, EquipmentSlot.HEAD,
						new FabricItemSettings().rarity(Rarity.RARE).maxDamage(128).group(ItemGroup.MISC)));
		SLIDER = Registry.register(Registry.SOUND_EVENT, SLIDER_ID, new SoundEvent(SLIDER_ID));
		MUSIC_DISC_SLIDER = Registry.register(Registry.ITEM, new Identifier(ID, "music_disc_slider"), new MusicDiscItem(14, SLIDER, (new Item.Settings()).maxCount(1).group(ItemGroup.MISC).rarity(Rarity.RARE)){});
		
	}
}
