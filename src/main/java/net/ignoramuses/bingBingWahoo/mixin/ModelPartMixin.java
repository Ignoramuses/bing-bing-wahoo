package net.ignoramuses.bingBingWahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.WahooUtils.ModelPartExtensions;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ModelPart.class)
public class ModelPartMixin implements ModelPartExtensions {
	@Unique
	private MatrixStack.Entry wahoo$lastEntry;
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V")
	private void wahoo$renderCap(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
		this.wahoo$lastEntry = matrices.peek();
	}
	
	@Override
	public MatrixStack.Entry wahoo$getLastMatrixStackEntry() {
		return this.wahoo$lastEntry;
	}
}
