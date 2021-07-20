package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ArmorStandArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class MysteriousCapFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
	private final MysteriousCapModel model;
	
	public MysteriousCapFeatureRenderer(FeatureRendererContext<T, M> context, MysteriousCapModel model) {
		super(context);
		this.model = model;
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		boolean wearingHat = false;
		ItemStack hatStack = null;
		for (ItemStack stack : entity.getArmorItems()) {
			if (stack.getItem().equals(BingBingWahoo.MYSTERIOUS_CAP)) {
				wearingHat = true;
				hatStack = stack;
			}
		}
		if (wearingHat) {
			int color = BingBingWahoo.MYSTERIOUS_CAP.getColor(hatStack);
			if (color == 10511680) { // don't want leather color
				color = 0xFFFFFF;
			}
			float r = (color >> 16 & 255) / 255.0F;
			float g = (color >> 8 & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			matrices.push();
			if (model.wearerModel instanceof BipedEntityModel bipedModel) {
				bipedModel.head.rotate(matrices);
			}
			matrices.translate(0, -1.8, -0.1);
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
			model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap.png"))), light, 1, r, g, b, 1);
			model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap_emblem.png"))), light, 1, 1, 1, 1, 1);
			matrices.pop();
		}
	}
}
