package io.github.ignoramuses.bing_bing_wahoo.content.cap.behavior;

import net.minecraft.client.player.LocalPlayer;

public class WahooLogic {
	public final LocalPlayer player;

	public final SimpleJumps simpleJumps = new SimpleJumps(this);

	public WahooLogic(LocalPlayer player) {
		this.player = player;
	}

	public void startTick() {
		simpleJumps.tick();
	}

	public void endTick() {

	}

	public void jump() {
		simpleJumps.jump();
	}
}
