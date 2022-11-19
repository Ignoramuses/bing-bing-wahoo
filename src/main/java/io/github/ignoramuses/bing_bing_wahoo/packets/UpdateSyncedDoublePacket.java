package io.github.ignoramuses.bing_bing_wahoo.packets;

import io.github.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import io.github.ignoramuses.bing_bing_wahoo.synced_config.SyncedConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;

import java.util.List;

public class UpdateSyncedDoublePacket {
	public static final ResourceLocation ID = BingBingWahoo.id("update_double_gamerule");

	public static void sendToAll(String ruleName, MinecraftServer server, DoubleRule value) {
		List<ServerPlayer> players = server.getPlayerList().getPlayers();
		FriendlyByteBuf buf = makeBuf(ruleName, value.get());
		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, ID, buf);
		}
	}

	public static void send(PacketSender sender, SyncedConfig<Double, DoubleRule> config, GameRules rules) {
		FriendlyByteBuf buf = makeBuf(config.name, rules.getRule(config.ruleKey).get());
		sender.sendPacket(ID, buf);
	}

	private static FriendlyByteBuf makeBuf(String ruleName, double value) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUtf(ruleName);
		buf.writeDouble(value);
		return buf;
	}

	@Environment(EnvType.CLIENT)
	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
			String name = buf.readUtf();
			double value = buf.readDouble();
			client.execute(() -> {
				SyncedConfig<Double, DoubleRule> config = SyncedConfig.DOUBLE_CONFIGS.get(name);
				if (config == null) {
					BingBingWahoo.LOGGER.error("Unknown synced config: " + name);
				} else {
					config.currentRuleValue = value;
				}
			});
		});
	}
}
