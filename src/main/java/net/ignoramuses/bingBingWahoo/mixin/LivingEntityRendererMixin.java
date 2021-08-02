package net.ignoramuses.bingBingWahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.ClientPlayerEntityExtensions;
import net.ignoramuses.bingBingWahoo.MysteriousCapFeatureRenderer;
import net.ignoramuses.bingBingWahoo.MysteriousCapModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
	@Shadow
	protected M model;
	
	protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
		super(ctx);
	}
	
	@Shadow
	protected abstract boolean addFeature(FeatureRenderer<T, M> feature);
	
	@Inject(at = @At("RETURN"), method = "<init>")
	public void wahoo$LivingEntityRenderer(EntityRendererFactory.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
		addFeature(new MysteriousCapFeatureRenderer(this, new MysteriousCapModel(ctx, this.model)));
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F"), method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	private void render(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (livingEntity instanceof ClientPlayerEntityExtensions extendedPlayer) {
			model.riding = livingEntity.hasVehicle() || extendedPlayer.slidingOnSlope() || extendedPlayer.slidingOnGround();
			if (extendedPlayer.slidingOnGround()) {
				matrixStack.translate(0, -0.25, 0);
			} else if (extendedPlayer.slidingOnSlope()) {
				matrixStack.translate(0, -0.5, 0);
			}
		}
	}
}
