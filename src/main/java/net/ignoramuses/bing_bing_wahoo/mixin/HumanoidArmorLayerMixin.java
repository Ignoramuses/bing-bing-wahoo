package net.ignoramuses.bing_bing_wahoo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
	public HumanoidArmorLayerMixin(RenderLayerParent<T, M> context) {
		super(context);
	}
	
	@Inject(at = @At("HEAD"), method = "renderArmorPiece", cancellable = true)
	private void wahoo$renderArmorPiece(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
		if (armorSlot == EquipmentSlot.HEAD) {
			ItemStack itemStack = entity.getItemBySlot(armorSlot);
			if (itemStack.is(BingBingWahoo.MYSTERIOUS_CAP)) {
				ci.cancel();
			}
		}
	}
}
