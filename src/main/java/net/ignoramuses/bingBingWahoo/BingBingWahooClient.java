package net.ignoramuses.bingBingWahoo;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static final Map<String, Object> GAME_RULES = new HashMap<>();
	public static BingBingWahooConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(BingBingWahooConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BingBingWahooConfig.class).getConfig();
		
		ClientPlayNetworking.registerGlobalReceiver(BingBingWahoo.UPDATE_BOOLEAN_GAMERULE_PACKET, (client, handler, buf, responseSender) -> {
			String gameRuleName = buf.readString();
			boolean value = buf.readBoolean();
			client.execute(() -> GAME_RULES.put(gameRuleName, value));
		});
		ClientPlayNetworking.registerGlobalReceiver(BingBingWahoo.UPDATE_DOUBLE_GAMERULE_PACKET, (client, handler, buf, responseSender) -> {
			String gameRuleName = buf.readString();
			double value = buf.readDouble();
			client.execute(() -> GAME_RULES.put(gameRuleName, value));
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
		EntityModelLayerRegistry.registerModelLayer(MysteriousCapModel.MODEL_LAYER, MysteriousCapModel::getTexturedModelData);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> BingBingWahoo.MYSTERIOUS_CAP.getColor(stack) == 10511680 ? 0xFFFFFF : BingBingWahoo.MYSTERIOUS_CAP.getColor(stack), BingBingWahoo.MYSTERIOUS_CAP);
		ItemTooltipCallback.EVENT.register(((stack, context, lines) -> {
			if (stack.isOf(BingBingWahoo.MYSTERIOUS_CAP)) {
				if (BingBingWahoo.MYSTERIOUS_CAP.getColor(stack) == 0x80C71F) {
					lines.remove(1);
					lines.add(1, new TranslatableText("bingbingwahoo.luigiNumberOne"));
				}
			} else if (stack.isOf(BingBingWahoo.MUSIC_DISC_SLIDER)) {
				lines.add(2, new TranslatableText("item.bingbingwahoo.music_disc_slider.desc2").formatted(Formatting.GRAY));
			}
		}));
	}
}
