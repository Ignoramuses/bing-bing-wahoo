package net.ignoramuses.bingBingWahoo.cap;

import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.WahooUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class FlyingCapRenderer extends EntityRenderer<FlyingCapEntity> {
	public static final Identifier TEXTURE = new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap.png");
	
	private final MysteriousCapModel model;
	
	public FlyingCapRenderer(Context context) {
		super(context);
		this.model = new MysteriousCapModel(context, null);
	}
	
	@Override
	public void render(FlyingCapEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
		ItemStack stack = entity.getStack();
		if (stack == null || stack.isEmpty()) return;
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
		matrices.translate(0, -1.55, 0);
		float rotation = (entity.age + tickDelta) * 50;
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotation));
		if (entity.ticksAtEnd > 0 && entity.ticksAtEnd <= 10) matrices.scale(2.5f, 1, 2.5f); // larger surface to jump on
		WahooUtils.renderCap(matrices, vertexConsumers, stack, light, tickDelta, model);
		matrices.pop();
	}
	
	@Override
	public Identifier getTexture(FlyingCapEntity entity) {
		return TEXTURE;
	}
}
