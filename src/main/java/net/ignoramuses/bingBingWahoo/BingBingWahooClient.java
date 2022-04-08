package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapRenderer;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapModel;
import net.ignoramuses.bingBingWahoo.compat.TrinketsHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.FLYING_CAP;
import static net.ignoramuses.bingBingWahoo.BingBingWahoo.TRINKETS_LOADED;
import static net.ignoramuses.bingBingWahoo.WahooCommands.UPDATE_DOUBLE_GAMERULE_PACKET;
import static net.ignoramuses.bingBingWahoo.WahooNetworking.*;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static final Map<String, Object> GAME_RULES = new HashMap<>();
	public static BingBingWahooConfig CONFIG;
	public static KeyMapping THROW_CAP = new KeyMapping("bingbingwahoo.key.throw_cap", GLFW.GLFW_KEY_G, "bingbingwahoo.key.category");
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(BingBingWahooConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BingBingWahooConfig.class).getConfig();
		
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
			CompoundTag data = buf.readNbt();
			String id = buf.readUtf();
			client.execute(() -> {
				FlyingCapEntity cap = null;
				for (Entity entity : client.level.entitiesForRendering()) {
					if (entity instanceof FlyingCapEntity && entity.getStringUUID().equals(id)) {
						cap = (FlyingCapEntity) entity;
						break;
					}
				}
				if (cap != null) {
					cap.readAdditionalSaveData(data);
				}
			});
		});
//		ClientPlayNetworking.registerGlobalReceiver(BingBingWahoo.BONK_PACKET, (client, handler, buf, sender) -> {
//			boolean start = buf.readBoolean();
//			UUID bonked = buf.readUuid();
//			client.execute(() -> {
//				PlayerEntity bonkedPlayer = client.world.getPlayerByUuid(bonked);
//				if (start) {
//					bonkedPlayer.setPose(EntityPose.SLEEPING);
//					((PlayerEntityExtensions) bonkedPlayer).setBonked(true, bonked);
//				} else {
//					((PlayerEntityExtensions) bonkedPlayer).setBonked(false, bonked);
//					bonkedPlayer.setPose(EntityPose.STANDING);
//				}
//			});
//		});
		EntityRendererRegistry.register(FLYING_CAP, FlyingCapRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(MysteriousCapModel.MODEL_LAYER, MysteriousCapModel::getTexturedModelData);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
				tintIndex == 0
						? BingBingWahoo.MYSTERIOUS_CAP.getColor(stack)
						: 0xFFFFFF,
				BingBingWahoo.MYSTERIOUS_CAP);
		
		ItemTooltipCallback.EVENT.register(((stack, context, lines) -> {
			if (stack.is(BingBingWahoo.MYSTERIOUS_CAP)) {
				if (BingBingWahoo.MYSTERIOUS_CAP.getColor(stack) == 0x80C71F) {
					lines.add(1, new TranslatableComponent("bingbingwahoo.luigiNumberOne"));
				}
			}
		}));
		
		KeyBindingHelper.registerKeyBinding(THROW_CAP);
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
}
