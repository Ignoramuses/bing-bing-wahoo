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
		matrices.push();
		model.wearerHead.rotate(matrices);
		final AtomicDouble x = new AtomicDouble(0);
		final AtomicDouble y = new AtomicDouble(0);
		final AtomicDouble z = new AtomicDouble(0);
		final AtomicDouble lowest = new AtomicDouble(Integer.MAX_VALUE);
		final AtomicDouble highest = new AtomicDouble(Integer.MIN_VALUE);
		model.wearerHead.forEachCuboid(matrices, (matrix, path, index, cuboid) -> {
			if (path == null || path.isEmpty() || path.contains("head")) {
				x.set(cuboid.maxX - cuboid.minX);
				y.set(cuboid.maxY - cuboid.minY);
				z.set(cuboid.maxZ - cuboid.minZ);
			}
			if (cuboid.minY < lowest.get()) {
				lowest.set(cuboid.minY);
			}
			if (cuboid.maxY > highest.get()) {
				highest.set(cuboid.maxY);
			}
		});
		double totalHeight = highest.get() - lowest.get();
		double size = Math.min(x.get(), Math.min(y.get(), z.get()));
//		matrices.scale((float) size, (float) size, (float) size);
		matrices.translate(0, -1.9, 0);
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap.png"))), light, 1, r, g, b, 1);
		model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap_emblem.png"))), light, 1, 1, 1, 1, 1);
		matrices.pop();
	}
}
