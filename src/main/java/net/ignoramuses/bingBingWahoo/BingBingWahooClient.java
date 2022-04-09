package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ignoramuses.bingBingWahoo.cap.CapPickupType;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapRenderer;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapModel;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.GameRules;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.FLYING_CAP;
import static net.ignoramuses.bingBingWahoo.BingBingWahoo.TRINKETS_LOADED;
import static net.ignoramuses.bingBingWahoo.WahooCommands.UPDATE_DOUBLE_GAMERULE_PACKET;
import static net.ignoramuses.bingBingWahoo.WahooNetworking.*;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	private static final Map<String, Object> GAME_RULES = new HashMap<>();
	public static BingBingWahooConfig CONFIG;
	public static KeyMapping THROW_CAP = new KeyMapping("bingbingwahoo.key.throw_cap", GLFW.GLFW_KEY_G, "bingbingwahoo.key.category");
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(BingBingWahooConfig.class, GsonConfigSerializer::new);
		ConfigHolder<BingBingWahooConfig> holder = AutoConfig.getConfigHolder(BingBingWahooConfig.class);
		CONFIG = holder.getConfig();
		holder.registerSaveListener((configHolder, bingBingWahooConfig) -> {
			CapPickupType type = bingBingWahooConfig.capPickupType;
			BingBingWahoo.PLAYERS_TO_TYPES.put(Minecraft.getInstance().player.getStringUUID(), type);
			int ordinal = type.ordinal();
			ClientPlayNetworking.send(UPDATE_PICKUP_TYPE, PacketByteBufs.create().writeVarInt(ordinal));
			return InteractionResult.PASS;
		});

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

		EntityRendererRegistry.register(FLYING_CAP, FlyingCapRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(MysteriousCapModel.MODEL_LAYER, MysteriousCapModel::getTexturedModelData);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
				tintIndex == 0
						? BingBingWahoo.MYSTERIOUS_CAP.getColor(stack)
						: 0xFFFFFF,
				BingBingWahoo.MYSTERIOUS_CAP);

		KeyBindingHelper.registerKeyBinding(THROW_CAP);

		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) ->
				sender.sendPacket(UPDATE_PICKUP_TYPE, PacketByteBufs.create().writeVarInt(CONFIG.capPickupType.ordinal()))));

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			while (THROW_CAP.consumeClick()) {
				LocalPlayer player = client.player;
				if (player != null) {
					FriendlyByteBuf buf = null;
					if (TRINKETS_LOADED && TrinketsHandler.capEquipped(player)) {
						buf = PacketByteBufs.create();
						buf.writeBoolean(true);
					} else if (player.getItemBySlot(EquipmentSlot.HEAD).is(BingBingWahoo.MYSTERIOUS_CAP)) {
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
