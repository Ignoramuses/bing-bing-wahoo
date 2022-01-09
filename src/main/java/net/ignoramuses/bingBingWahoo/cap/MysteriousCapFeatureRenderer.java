package net.ignoramuses.bingBingWahoo.cap;

import com.google.common.util.concurrent.AtomicDouble;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;

@Environment(EnvType.CLIENT)
public class MysteriousCapFeatureRenderer<T extends Entity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
	public static final Identifier CAP = new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap.png");
	public static final Identifier EMBLEM = new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap_emblem.png");
	
	private final MysteriousCapModel model;
	
	public MysteriousCapFeatureRenderer(FeatureRendererContext<T, M> context, MysteriousCapModel model) {
		super(context);
		this.model = model;
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (!(entity instanceof CapWearer wearer) || !wearer.isWearingCap() || !model.gotHead()) return;
		ItemStack hatStack = wearer.getCap();
		
		int color = MYSTERIOUS_CAP.getColor(hatStack);
		if (color == 10511680) { // don't want leather color
			color = 0xFFFFFF;
		}
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
//		matrices.push();
		model.wearerHead.rotate(matrices);
		final AtomicDouble xLow = new AtomicDouble(Double.MAX_VALUE);
		final AtomicDouble xHigh = new AtomicDouble(Double.MIN_VALUE);
		final AtomicDouble yLow = new AtomicDouble(Double.MAX_VALUE);
		final AtomicDouble yHigh = new AtomicDouble(Double.MIN_VALUE);
		final AtomicDouble zLow = new AtomicDouble(Double.MAX_VALUE);
		final AtomicDouble zHigh = new AtomicDouble(Double.MIN_VALUE);
		model.wearerHead.forEachCuboid(matrices, (matrix, path, index, cuboid) -> {
			if (path == null || path.isEmpty() || path.equals("/") || path.contains("head")) {
				xLow.set(cuboid.minX);
				xHigh.set(cuboid.maxX);
				yLow.set(cuboid.minY);
				yHigh.set(cuboid.maxY);
				zLow.set(cuboid.minZ);
				zHigh.set(cuboid.maxZ);
			}
//			if (cuboid.minY < lowest.get()) {
//				lowest.set(cuboid.minY);
//			}
//			if (cuboid.maxY > highest.get()) {
//				highest.set(cuboid.maxY);
//			}
		});
//		double totalHeight = highest.get() - lowest.get();
//		double size = 1 / Math.min(x.get(), Math.min(y.get(), z.get()));
//		matrices.scale((float) size, (float) size, (float) size);
		
		matrices.push();
		matrices.translate(xLow.get(), yLow.get(), zLow.get());
		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(CAP)), light, 1, r, g, b, 1);
		matrices.pop();
		matrices.push();
		matrices.translate(xHigh.get(), yHigh.get(), zHigh.get());
		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(CAP)), light, 1, r, g, b, 1);
		matrices.pop();
//		matrices.translate(0, -1.9, 0);
//		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
//		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(CAP)), light, 1, r, g, b, 1);
//		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(EMBLEM)), light, 1, 1, 1, 1, 1);
//		matrices.pop();
	}
}
