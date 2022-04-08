package net.ignoramuses.bingBingWahoo.cap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MysteriousCapItem extends DyeableArmorItem {
	public MysteriousCapItem(ArmorMaterial armorMaterial, EquipmentSlot equipmentSlot, Properties settings) {
		super(armorMaterial, equipmentSlot, settings);
	}
	
	public int getColor(ItemStack stack) {
		CompoundTag nbtCompound = stack.getTagElement("display");
		return nbtCompound != null && nbtCompound.contains("color", 99) ? nbtCompound.getInt("color") : 0xFFFFFF;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack held = user.getItemInHand(hand);
		boolean client = world.isClientSide();
		if (!client) {
			FlyingCapEntity.spawn((ServerPlayer) user, held, PreferredCapSlot.HAND);
			user.setItemInHand(hand, ItemStack.EMPTY);
		}
		return InteractionResultHolder.sidedSuccess(held, client);
	}
}
