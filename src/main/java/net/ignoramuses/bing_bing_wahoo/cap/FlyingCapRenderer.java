package net.ignoramuses.bing_bing_wahoo.cap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.ignoramuses.bing_bing_wahoo.WahooUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FlyingCapRenderer extends EntityRenderer<FlyingCapEntity> {
	public static final ResourceLocation TEXTURE = new ResourceLocation(BingBingWahoo.ID, "textures/armor/mysterious_cap.png");
	
	private final MysteriousCapModel model;
	
	public FlyingCapRenderer(Context context) {
		super(context);
		this.model = new MysteriousCapModel(context, null);
	}
	
	@Override
	public void render(FlyingCapEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		ItemStack stack = entity.getItem();
		if (stack == null || stack.isEmpty()) return;
		matrices.pushPose();
		matrices.mulPose(Vector3f.ZP.rotationDegrees(180));
		matrices.translate(0, -1.55, 0);
		float rotation = (entity.tickCount + tickDelta) * 50;
		matrices.mulPose(Vector3f.YP.rotationDegrees(rotation));
//		if (entity.ticksAtEnd > 0 && entity.ticksAtEnd <= 10) matrices.scale(2.5f, 1, 2.5f); // larger surface to jump on
		WahooUtils.renderCap(matrices, vertexConsumers, stack, light, tickDelta, model);
		matrices.popPose();
	}
	
	@Override
	public ResourceLocation getTextureLocation(FlyingCapEntity entity) {
		return TEXTURE;
	}
}
