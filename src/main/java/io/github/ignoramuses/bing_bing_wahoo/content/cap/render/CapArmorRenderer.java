package io.github.ignoramuses.bing_bing_wahoo.content.cap.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.registry.WahooItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class CapArmorRenderer implements ArmorRenderer {
	public static final CapArmorRenderer INSTANCE = new CapArmorRenderer();
	public static final ResourceLocation TEXTURE = BingBingWahoo.id("textures/armor/mysterious_cap.png");
	private static final Quaternionf rot90Clockwise = Axis.YP.rotationDegrees(90);
	protected final CapModel model = new CapModel();

	@Override
	public void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, LivingEntity entity,
					   EquipmentSlot slot, int light, HumanoidModel<LivingEntity> entityModel) {
		matrices.pushPose();
		entityModel.head.translateAndRotate(matrices);
		matrices.translate(0, -1.8, -0.125);
		matrices.mulPose(rot90Clockwise);

		RenderType renderType = RenderType.armorCutoutNoCull(TEXTURE);
		VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, renderType, false, stack.hasFoil());

		int color = WahooItems.MYSTERIOUS_CAP.getColor(stack);
		float r = (color >> 16 & 0xFF) / 255.0F;
		float g = (color >> 8 & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;
		model.renderToBuffer(matrices, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1);

		matrices.popPose();
	}
}
