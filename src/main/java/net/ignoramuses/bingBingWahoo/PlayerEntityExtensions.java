package net.ignoramuses.bingBingWahoo;

import java.util.UUID;

public interface PlayerEntityExtensions {
	void setBonked(boolean value, UUID bonked);
	boolean getSliding();
}
