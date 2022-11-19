package net.ignoramuses.bing_bing_wahoo;

import eu.midnightdust.lib.config.MidnightConfig;
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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ignoramuses.bing_bing_wahoo.cap.FlyingCapEntity;
import net.ignoramuses.bing_bing_wahoo.cap.FlyingCapRenderer;
import net.ignoramuses.bing_bing_wahoo.cap.MysteriousCapModel;
import net.ignoramuses.bing_bing_wahoo.compat.TrinketsCompat;
import net.ignoramuses.bing_bing_wahoo.extensions.AbstractClientPlayerExtensions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.GameRules;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.ignoramuses.bing_bing_wahoo.WahooCommands.UPDATE_DOUBLE_GAMERULE_PACKET;
import static net.ignoramuses.bing_bing_wahoo.WahooNetworking.*;

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
		ClientPlayNetworking.registerGlobalReceiver(CAP_ENTITY_SPAWN, (client, handler, buf, sender) -> {
			ClientboundAddEntityPacket addPacket = new ClientboundAddEntityPacket(buf);
			CompoundTag data = buf.readNbt();
			client.execute(() -> {
				addPacket.handle(handler);
				FlyingCapEntity cap = (FlyingCapEntity) client.level.getEntity(addPacket.getId());
				if (cap != null) {
					cap.readAdditionalSaveData(data);
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(UPDATE_FLIP, (client, handler, buf, responseSender) -> {
			boolean started = buf.readBoolean();
			boolean forwards = started && buf.readBoolean();
			UUID id = buf.readUUID();
			client.execute(() -> {
				for (AbstractClientPlayer player : client.level.players()) {
					if (player.getGameProfile().getId().equals(id)) {
						((AbstractClientPlayerExtensions) player).wahoo$setFlipping(started);
						if (started) ((AbstractClientPlayerExtensions) player).wahoo$setFlipDirection(forwards);
						break;
					}
				}
			});
		});

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
					FriendlyByteBuf buf = null;
					if (TrinketsCompat.capTrinketEquipped(player)) {
						buf = PacketByteBufs.create();
						buf.writeBoolean(true);
					} else if (player.getItemBySlot(EquipmentSlot.HEAD).is(WahooRegistry.MYSTERIOUS_CAP)) {
						buf = PacketByteBufs.create();
						buf.writeBoolean(false);
					}
					if (buf != null)
						ClientPlayNetworking.send(WahooNetworking.CAP_THROW, buf);
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
