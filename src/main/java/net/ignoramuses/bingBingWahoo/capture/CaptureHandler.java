package net.ignoramuses.bingBingWahoo.capture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;

public interface CaptureHandler<T extends Entity> {
	default void onCapture(T captured, PlayerEntity player) {}
	default void onInteract(T captured, PlayerEntity player, HitResult hitResult) {}
	default void onAttack(T captured, PlayerEntity player, HitResult hitResult) {}
	
	/**
	 * Transform the cap model to position it correctly on the entity's head.
	 */
	@Environment(EnvType.CLIENT)
	default void transform(MatrixStack matrices, T captured, PlayerEntity player, EntityModel<T> model) {}
}
