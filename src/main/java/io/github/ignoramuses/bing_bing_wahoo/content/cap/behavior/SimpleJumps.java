package io.github.ignoramuses.bing_bing_wahoo.content.cap.behavior;

public class SimpleJumps extends Module {
	private int ticksToDoubleJump = 0;
	private int ticksToTripleJump = 0;

	public SimpleJumps(WahooLogic logic) {
		super(logic);
	}

	public void tick() {
		ticksToDoubleJump--;
		ticksToTripleJump--;
	}


	private boolean canDoubleJump() {
		return ticksToDoubleJump > 0;
	}

	private boolean canTripleJump() {
		return ticksToTripleJump > 0;
	}

	private boolean canSingleJump() {
		return true;
	}

	public void jump() {
		if (canDoubleJump()) {
			System.out.println("remaining ticksToDoubleJump: " + ticksToDoubleJump);
			ticksToDoubleJump = 0;
			ticksToTripleJump = 40;
		} else if (canTripleJump()) {
			System.out.println("remaining ticksToTripleJump: " + ticksToTripleJump);
			ticksToTripleJump = 0;
		} else if (canSingleJump()) {
			ticksToDoubleJump = 20;
		}
	}

	public float powerMultiplierForNextJump() {
		if (canDoubleJump()) {
			return 1.25f;
		} else if (canTripleJump()) {
			return 2;
		} else {
			return 1;
		}
	}
}
