package net.ignoramuses.bingBingWahoo.cap;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MysteriousCapItem extends DyeableArmorItem {
	public MysteriousCapItem(ArmorMaterial armorMaterial, EquipmentSlot equipmentSlot, Settings settings) {
		super(armorMaterial, equipmentSlot, settings);
	}
	
	public int getColor(ItemStack stack) {
		NbtCompound nbtCompound = stack.getSubNbt("display");
		return nbtCompound != null && nbtCompound.contains("color", 99) ? nbtCompound.getInt("color") : 0xFFFFFF;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack held = user.getStackInHand(hand);
		boolean client = world.isClient();
		if (!client) {
			FlyingCapEntity.spawn((ServerPlayerEntity) user, held, PreferredCapSlot.HAND);
			user.setStackInHand(hand, ItemStack.EMPTY);
		}
		return TypedActionResult.success(held, client);
	}
}
