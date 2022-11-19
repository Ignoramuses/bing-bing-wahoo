package io.github.ignoramuses.bing_bing_wahoo.synced_config;

import io.github.ignoramuses.bing_bing_wahoo.packets.UpdateSyncedDoublePacket;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.server.MinecraftServer;

import java.util.function.BiConsumer;

public record SyncedDoubleUpdater(String ruleName) implements BiConsumer<MinecraftServer, DoubleRule> {
	@Override
	public void accept(MinecraftServer server, DoubleRule value) {
		UpdateSyncedDoublePacket.sendToAll(ruleName, server, value);
	}
}
