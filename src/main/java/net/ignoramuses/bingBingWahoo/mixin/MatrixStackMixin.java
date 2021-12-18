package net.ignoramuses.bingBingWahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.WahooUtils.MatrixStackExtensions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Deque;

@Environment(EnvType.CLIENT)
@Mixin(MatrixStack.class)
public class MatrixStackMixin implements MatrixStackExtensions {
	
	@Shadow
	@Final
	private Deque<Entry> stack;
	
	@Override
	public void wahoo$push(Entry entry) {
		stack.addLast(entry);
	}
}
