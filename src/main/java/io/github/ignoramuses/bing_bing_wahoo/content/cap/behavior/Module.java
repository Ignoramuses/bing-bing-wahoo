package io.github.ignoramuses.bing_bing_wahoo.content.cap.behavior;

public abstract class Module {
	protected final WahooLogic logic;

	public Module(WahooLogic logic) {
		this.logic = logic;
	}
}
