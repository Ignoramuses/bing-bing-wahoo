package io.github.ignoramuses.bing_bing_wahoo.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.render.CapArmorRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CapTrinketRenderer implements TrinketRenderer {
	public static final CapTrinketRenderer INSTANCE = new CapTrinketRenderer();

	@Override
	public void render(ItemStack stack, SlotReference slot, EntityModel<? extends LivingEntity> entityModel,
					   PoseStack matrices, MultiBufferSource vertexConsumers, int light, LivingEntity entity, float limbAngle,
					   float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (entityModel instanceof HumanoidModel<?> humanoidModel) {
			//noinspection unchecked - AAAAAAAAAAAAAA
			HumanoidModel<LivingEntity> model = (HumanoidModel<LivingEntity>) humanoidModel;
			CapArmorRenderer.INSTANCE.render(matrices, vertexConsumers, stack, entity, EquipmentSlot.HEAD, light, model);
		}
	}
}
