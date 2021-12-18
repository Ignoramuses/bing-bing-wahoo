package net.ignoramuses.bingBingWahoo.mixin;

import com.google.common.util.concurrent.AtomicDouble;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.WahooUtils.ModelPartExtensions;
import net.ignoramuses.bingBingWahoo.cap.CapWearer;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;

@Environment(EnvType.CLIENT)
@Mixin(ModelPart.class)
public class ModelPartMixin implements ModelPartExtensions {
	@Nullable
	private MysteriousCapModel wahoo$capModel;
	private Entity wahoo$entity;
	private VertexConsumerProvider wahoo$vertexConsumerProvider;
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V")
	private void wahoo$renderCap(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
		if (this.wahoo$capModel != null) {
			if (!(wahoo$entity instanceof CapWearer wearer) || !wearer.isWearingCap()) return;
			ItemStack hatStack = wearer.getCap();
			
			int color = MYSTERIOUS_CAP.getColor(hatStack);
			if (color == 10511680) { // don't want leather color
				color = 0xFFFFFF;
			}
			float r = (color >> 16 & 255) / 255.0F;
			float g = (color >> 8 & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			matrices.push();
			
//			matrices.scale((float) size, (float) size, (float) size);
			matrices.translate(0, -1.7, 0);
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
			wahoo$capModel.render(matrices, wahoo$vertexConsumerProvider.getBuffer(RenderLayer.getArmorCutoutNoCull(new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap.png"))), light, 1, r, g, b, 1);
			wahoo$capModel.render(matrices, wahoo$vertexConsumerProvider.getBuffer(RenderLayer.getArmorCutoutNoCull(new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap_emblem.png"))), light, 1, 1, 1, 1, 1);
			matrices.pop();
		}
	}
	
	@Override
	public void setCapRenderer(MysteriousCapModel model) {
		this.wahoo$capModel = model;
	}
	
	@Override
	public void setEntity(Entity entity) {
		this.wahoo$entity = entity;
	}
	
	@Override
	public void setVertexConsumerProvider(VertexConsumerProvider provider) {
		this.wahoo$vertexConsumerProvider = provider;
	}
}
