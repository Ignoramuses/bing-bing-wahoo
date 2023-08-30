package io.github.ignoramuses.bing_bing_wahoo.content.cap.behavior;

/**
 * Double and triple jumps.
 * Doing these requires releasing space between jumps, can't just hold it
 * First jump gives 20 ticks to do a second, which gives 30 ticks to do a third
 */
public class SimpleJumps extends Module {
	private int ticksToDoubleJump = 0;
	private int ticksToTripleJump = 0;
	private boolean jumpHeldSinceJump = false;

	public SimpleJumps(WahooLogic logic) {
		super(logic);
	}

	public void tick() {
		ticksToDoubleJump--;
		ticksToTripleJump--;
		jumpHeldSinceJump &= logic.player.input.jumping;
	}

	private boolean canSingleJump() {
		return !canDoubleJump() && !canTripleJump();
	}

	private boolean canDoubleJump() {
		return !jumpHeldSinceJump && ticksToDoubleJump > 0;
	}

	private boolean canTripleJump() {
		return !jumpHeldSinceJump && ticksToTripleJump > 0;
	}

	public void afterJump() {
		if (canSingleJump()) {
			ticksToDoubleJump = 20;
		} else if (canDoubleJump()) {
			ticksToDoubleJump = 0;
			ticksToTripleJump = 30;
		} else if (canTripleJump()) {
			ticksToTripleJump = 0;
		}
		jumpHeldSinceJump = true;
	}

	public float powerMultiplierForNextJump() {
		if (canDoubleJump()) {
			return 1.5f;
		} else if (canTripleJump()) {
			return 2.1f;
		} else {
			return 1f;
		}
	}
}
