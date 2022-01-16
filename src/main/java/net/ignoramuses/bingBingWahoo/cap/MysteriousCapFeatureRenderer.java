package net.ignoramuses.bingBingWahoo.cap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.WahooUtils;
import net.ignoramuses.bingBingWahoo.capture.CaptureHandler;
import net.ignoramuses.bingBingWahoo.capture.CapturingRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;

@Environment(EnvType.CLIENT)
public class MysteriousCapFeatureRenderer<T extends Entity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
	private final MysteriousCapModel model;
	
	public MysteriousCapFeatureRenderer(FeatureRendererContext<T, M> context, MysteriousCapModel model) {
		super(context);
		this.model = model;
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (model.wearerHead == null || !(entity instanceof CapWearer wearer) || !wearer.isWearingCap()) return;
		matrices.push();
		model.wearerHead.rotate(matrices);
		if (model.wearerModel instanceof BipedEntityModel) matrices.translate(0, -1.8, -0.1);
		WahooUtils.renderCap(matrices, vertexConsumers, wearer.getCap(), light, tickDelta, model);
		matrices.pop();
	}
}
