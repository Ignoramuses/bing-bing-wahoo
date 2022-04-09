package net.ignoramuses.bingBingWahoo;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SliderRecordItem extends RecordItem {
	public static final MutableComponent SLIDER_DESC_2 = new TranslatableComponent("item.bingbingwahoo.music_disc_slider.desc2")
			.withStyle(ChatFormatting.GRAY);

	public SliderRecordItem(int i, SoundEvent soundEvent, Properties properties) {
		super(i, soundEvent, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
		tooltipComponents.add(SLIDER_DESC_2);
	}
}
