package io.github.ignoramuses.bing_bing_wahoo;

import io.github.ignoramuses.bing_bing_wahoo.packets.*;
import io.github.ignoramuses.bing_bing_wahoo.synced_config.SyncedConfig;

public class WahooNetworking {
	public static void init() {
		CapThrowPacket.init();
		GroundPoundPacket.init();
		UpdatePreviousJumpTypePacket.init();
		StartFallFlyPacket.init();
		UpdateBonkPacket.init();
		UpdateDivePacket.init();
		UpdateFlipStatePacket.init();
		UpdatePosePacket.init();
		UpdateSlidePacket.init();

		SyncedConfig.init();
	}
}
