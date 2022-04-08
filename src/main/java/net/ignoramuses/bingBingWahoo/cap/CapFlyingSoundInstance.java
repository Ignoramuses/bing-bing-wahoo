package net.ignoramuses.bingBingWahoo.cap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class CapFlyingSoundInstance extends AbstractTickableSoundInstance {
	protected FlyingCapEntity capEntity;

	public CapFlyingSoundInstance(FlyingCapEntity capEntity) {
		super(SoundEvents.ELYTRA_FLYING, SoundSource.NEUTRAL);
		this.capEntity = capEntity;
		this.looping = true;
		this.delay = 0;
		this.volume = 0.1F;
		this.pitch = 1.75f;
	}

	@Override
	public void tick() {
		if (!capEntity.isRemoved() && !capEntity.isSilent()) {
			this.x = this.capEntity.getX();
			this.y = this.capEntity.getY();
			this.z = this.capEntity.getZ();
			LocalPlayer player = Minecraft.getInstance().player;
			float distance = (float) player.position().distanceTo(capEntity.position());
			System.out.println(distance);
			this.volume = 1 / distance;
		} else {
			stop();
		}
	}
}
