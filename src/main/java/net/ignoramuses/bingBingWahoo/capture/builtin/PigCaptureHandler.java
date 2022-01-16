package net.ignoramuses.bingBingWahoo.capture.builtin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.capture.CaptureHandler;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;

public enum PigCaptureHandler implements CaptureHandler<PigEntity> {
	INSTANCE;
	
	@Override
	public void onInteract(PigEntity captured, PlayerEntity player, HitResult hitResult) {
		oink(player);
	}
	
	@Override
	public void onAttack(PigEntity captured, PlayerEntity player, HitResult hitResult) {
		oink(player);
	}
	
	private void oink(PlayerEntity player) {
		player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PIG_AMBIENT, SoundCategory.AMBIENT, 1f, 1f);
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void transform(MatrixStack matrices, PigEntity captured, PlayerEntity player, EntityModel<PigEntity> model) {
	
	}
}
