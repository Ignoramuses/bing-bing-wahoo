package net.ignoramuses.bingBingWahoo.cap;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.WahooUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class MysteriousCapFeatureRenderer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final MysteriousCapModel model;
	
	public MysteriousCapFeatureRenderer(RenderLayerParent<T, M> context, MysteriousCapModel model) {
		super(context);
		this.model = model;
	}
	
	@Override
	public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (model.wearerHead == null || !(entity instanceof CapWearer wearer) || !wearer.isWearingCap()) return;
		ItemStack cap = wearer.getCap();
		if (cap.hasTag() && cap.getTag().contains("wahoo:skip_render")) {
			return;
		}
		matrices.pushPose();
		model.wearerHead.translateAndRotate(matrices);
		if (model.wearerModel instanceof HumanoidModel) matrices.translate(0, -1.8, -0.1);
		if (entity instanceof AbstractPiglin || entity instanceof ZombifiedPiglin)
			matrices.scale(1.2f, 1, 1);
		WahooUtils.renderCap(matrices, vertexConsumers, wearer.getCap(), light, tickDelta, model);
		matrices.popPose();
	}
}
