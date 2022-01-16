package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapArmorMaterial;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapItem;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
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
	public static DyeableArmorItem MYSTERIOUS_CAP;
	public static EntityType<FlyingCapEntity> FLYING_CAP;
	
	@Override
	public void onInitialize() {
		WahooNetworking.init();
		WahooCommands.init();
		MYSTERIOUS_CAP = Registry.register(Registry.ITEM, new Identifier(ID, "mysterious_cap"),
				new MysteriousCapItem(MysteriousCapArmorMaterial.INSTANCE, EquipmentSlot.HEAD,
						new FabricItemSettings().rarity(Rarity.RARE).maxDamage(128).group(ItemGroup.MISC)));
		FLYING_CAP = Registry.register(Registry.ENTITY_TYPE, new Identifier(ID, "flying_cap"),
				FabricEntityTypeBuilder.<FlyingCapEntity>create(SpawnGroup.MISC, FlyingCapEntity::new)
						.dimensions(EntityDimensions.fixed(0.75f, 0.5f))
						.fireImmune()
						.disableSummon()
						.build());
	}
}
