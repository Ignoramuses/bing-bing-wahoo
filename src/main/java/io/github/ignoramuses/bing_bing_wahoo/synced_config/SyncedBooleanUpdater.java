package io.github.ignoramuses.bing_bing_wahoo.synced_config;

import io.github.ignoramuses.bing_bing_wahoo.packets.UpdateSyncedBooleanPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules.BooleanValue;

import java.util.function.BiConsumer;

public record SyncedBooleanUpdater(String ruleName) implements BiConsumer<MinecraftServer, BooleanValue> {
	@Override
	public void accept(MinecraftServer server, BooleanValue value) {
		UpdateSyncedBooleanPacket.sendToAll(ruleName, server, value);
	}
}
