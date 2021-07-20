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
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.text.TranslatableText;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static BingBingWahooConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		AutoConfig.register(BingBingWahooConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BingBingWahooConfig.class).getConfig();
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
			if (stack.getItem().equals(BingBingWahoo.MYSTERIOUS_CAP)) {
				if (BingBingWahoo.MYSTERIOUS_CAP.getColor(stack) == 0x80C71F) {
					lines.remove(1);
					lines.add(1, new TranslatableText("bingbingwahoo.luigiNumberOne"));
				}
			}
		}));
	}
}
