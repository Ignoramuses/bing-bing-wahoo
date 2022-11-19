package io.github.ignoramuses.bing_bing_wahoo;

import eu.midnightdust.lib.config.MidnightConfig;
import io.github.ignoramuses.bing_bing_wahoo.packets.CapSpawnPacket;
import io.github.ignoramuses.bing_bing_wahoo.packets.CapThrowPacket;
import io.github.ignoramuses.bing_bing_wahoo.packets.UpdateFlipStatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapEntity;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapRenderer;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapModel;
import io.github.ignoramuses.bing_bing_wahoo.compat.TrinketsCompat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.GameRules;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static io.github.ignoramuses.bing_bing_wahoo.WahooCommands.UPDATE_DOUBLE_GAMERULE_PACKET;
import static io.github.ignoramuses.bing_bing_wahoo.WahooNetworking.*;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	private static final Map<String, Object> GAME_RULES = new HashMap<>();
	public static KeyMapping THROW_CAP = new KeyMapping("bingbingwahoo.key.throw_cap", GLFW.GLFW_KEY_G, "bingbingwahoo.key.category");
	
	@Override
	public void onInitializeClient() {
		MidnightConfig.init("bingbingwahoo", BingBingWahooConfig.class);

		ClientPlayNetworking.registerGlobalReceiver(UPDATE_BOOLEAN_GAMERULE_PACKET, (client, handler, buf, responseSender) -> {
			String gameRuleName = buf.readUtf();
			boolean value = buf.readBoolean();
			client.execute(() -> GAME_RULES.put(gameRuleName, value));
		});
		ClientPlayNetworking.registerGlobalReceiver(UPDATE_DOUBLE_GAMERULE_PACKET, (client, handler, buf, responseSender) -> {
			String gameRuleName = buf.readUtf();
			double value = buf.readDouble();
			client.execute(() -> GAME_RULES.put(gameRuleName, value));
		});

		CapSpawnPacket.clientInit();
		UpdateFlipStatePacket.initClient();

		EntityRendererRegistry.register(WahooRegistry.FLYING_CAP, FlyingCapRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(MysteriousCapModel.MODEL_LAYER, MysteriousCapModel::getTexturedModelData);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
				tintIndex == 0
						? WahooRegistry.MYSTERIOUS_CAP.getColor(stack)
						: 0xFFFFFF,
				WahooRegistry.MYSTERIOUS_CAP);

		KeyBindingHelper.registerKeyBinding(THROW_CAP);

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			while (THROW_CAP.consumeClick()) {
				LocalPlayer player = client.player;
				if (player != null) {
					boolean trinketEquipped = TrinketsCompat.capTrinketEquipped(player);
					// trinket takes priority
					boolean capEquipped = trinketEquipped || player.getItemBySlot(EquipmentSlot.HEAD).is(WahooRegistry.MYSTERIOUS_CAP);
					if (trinketEquipped || capEquipped) {
						CapThrowPacket.send(trinketEquipped);
					}
				}
			}
		});
	}

	public static boolean getBooleanValue(GameRules.Key<GameRules.BooleanValue> key) {
		String id = key.getId();
		Object value = GAME_RULES.get(id);
		if (value != null) return (boolean) value;
		return false;
	}

	public static double getDoubleValue(GameRules.Key<DoubleRule> key) {
		String id = key.getId();
		Object value = GAME_RULES.get(id);
		if (value != null) return (double) value;
		return 0;
	}
}
