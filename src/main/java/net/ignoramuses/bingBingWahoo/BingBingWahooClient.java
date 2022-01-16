package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapEntity;
import net.ignoramuses.bingBingWahoo.cap.FlyingCapRenderer;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapModel;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;

import java.util.HashMap;
import java.util.Map;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.FLYING_CAP;
import static net.ignoramuses.bingBingWahoo.WahooCommands.UPDATE_DOUBLE_GAMERULE_PACKET;
import static net.ignoramuses.bingBingWahoo.WahooNetworking.CAP_ENTITY_SPAWN;
import static net.ignoramuses.bingBingWahoo.WahooNetworking.UPDATE_BOOLEAN_GAMERULE_PACKET;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static final Map<String, Object> GAME_RULES = new HashMap<>();
	public static BingBingWahooConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(BingBingWahooConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BingBingWahooConfig.class).getConfig();
		
		ClientPlayNetworking.registerGlobalReceiver(UPDATE_BOOLEAN_GAMERULE_PACKET, (client, handler, buf, responseSender) -> {
			String gameRuleName = buf.readString();
			boolean value = buf.readBoolean();
			client.execute(() -> GAME_RULES.put(gameRuleName, value));
		});
		ClientPlayNetworking.registerGlobalReceiver(UPDATE_DOUBLE_GAMERULE_PACKET, (client, handler, buf, responseSender) -> {
			String gameRuleName = buf.readString();
			double value = buf.readDouble();
			client.execute(() -> GAME_RULES.put(gameRuleName, value));
		});
		ClientPlayNetworking.registerGlobalReceiver(CAP_ENTITY_SPAWN, (client, handler, buf, sender) -> {
			NbtCompound data = buf.readNbt();
			String id = buf.readString();
			client.execute(() -> {
				FlyingCapEntity cap = null;
				for (Entity entity : client.world.getEntities()) {
					if (entity instanceof FlyingCapEntity && entity.getUuidAsString().equals(id)) {
						cap = (FlyingCapEntity) entity;
						break;
					}
				}
				if (cap != null) {
					cap.readCustomDataFromNbt(data);
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
			if (stack.isOf(BingBingWahoo.MYSTERIOUS_CAP)) {
				if (BingBingWahoo.MYSTERIOUS_CAP.getColor(stack) == 0x80C71F) {
					lines.add(1, new TranslatableText("bingbingwahoo.luigiNumberOne"));
				}
			}
		}));
	}
}
