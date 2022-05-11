package net.ignoramuses.bingBingWahoo.cap;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MysteriousCapItem extends DyeableArmorItem {
	public static final MutableComponent LUIGI_NUMBER_ONE = new TranslatableComponent("bingbingwahoo.luigi_number_one")
			.withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN);

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
		if (!client && user instanceof ServerPlayer player) {
			FlyingCapEntity.spawn(player, held, PreferredCapSlot.fromHand(hand));
		}
		return InteractionResultHolder.sidedSuccess(held, client);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		if (player.isCrouching() && !entity.isBaby() && entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
			// all bipeds
			if (entity instanceof Zombie || entity instanceof EnderMan || entity instanceof AbstractPiglin) {
				if (!player.level.isClientSide()) {
					ItemStack toSet = stack.copy();
					toSet.setCount(1);
					entity.setItemSlot(EquipmentSlot.HEAD, toSet);
					if (!player.isCreative())
						stack.shrink(1);
				}
				player.level.playSound(null, entity.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.NEUTRAL, 1, 1);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
		if (getColor(stack) == 0x80C71F) {
			tooltipComponents.add(1, LUIGI_NUMBER_ONE);
		}
	}
}
