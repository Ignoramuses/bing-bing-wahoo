package io.github.ignoramuses.bing_bing_wahoo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapFeatureRenderer;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapModel;
import io.github.ignoramuses.bing_bing_wahoo.extensions.LocalPlayerExtensions;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
	@Shadow
	protected M model;
	
	@Shadow
	protected abstract boolean addLayer(RenderLayer<T, M> layer);
	
	protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Inject(at = @At("RETURN"), method = "<init>")
	public void wahoo$LivingEntityRenderer(EntityRendererProvider.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
		addLayer(new MysteriousCapFeatureRenderer<>(this, new MysteriousCapModel(ctx, this.model)));
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isBaby()Z"), method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
	private void wahoo$render(T livingEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, CallbackInfo ci) {
		if (livingEntity instanceof LocalPlayerExtensions extendedPlayer) {
			if (extendedPlayer.wahoo$slidingOnGround()) {
				model.riding = true;
				matrixStack.translate(0, -0.25, 0);
			} else if (extendedPlayer.wahoo$slidingOnSlope()) {
				model.riding = true;
				matrixStack.translate(0, -0.5, 0);
			}
		}
	}
}
