package net.ignoramuses.bingBingWahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.MysteriousCapFeatureRenderer;
import net.ignoramuses.bingBingWahoo.MysteriousCapModel;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
	@Shadow protected abstract boolean addFeature(FeatureRenderer<T, M> feature);
	
	@Shadow protected M model;
	
	protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
		super(ctx);
	}
	
	@Inject(at = @At("RETURN"), method = "<init>")
	public void wahoo$PlayerEntityRenderer(EntityRendererFactory.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
		addFeature(new MysteriousCapFeatureRenderer(this, new MysteriousCapModel(ctx, this.model)));
	}
}
