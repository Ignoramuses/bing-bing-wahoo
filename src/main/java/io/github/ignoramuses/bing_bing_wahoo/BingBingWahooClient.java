package io.github.ignoramuses.bing_bing_wahoo;

import eu.midnightdust.lib.config.MidnightConfig;
import io.github.ignoramuses.bing_bing_wahoo.packets.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.FlyingCapRenderer;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapModel;
import io.github.ignoramuses.bing_bing_wahoo.compat.TrinketsCompat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BingBingWahooClient implements ClientModInitializer {
	public static KeyMapping THROW_CAP = KeyBindingHelper.registerKeyBinding(
			new KeyMapping("bingbingwahoo.key.throw_cap", GLFW.GLFW_KEY_G, "bingbingwahoo.key.category")
	);
	
	@Override
	public void onInitializeClient() {
		MidnightConfig.init(BingBingWahoo.ID, BingBingWahooConfig.class);

		CapSpawnPacket.clientInit();
		UpdateFlipStatePacket.initClient();
		UpdateSyncedBooleanPacket.initClient();
		UpdateSyncedDoublePacket.initClient();

		EntityRendererRegistry.register(WahooRegistry.FLYING_CAP, FlyingCapRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(MysteriousCapModel.MODEL_LAYER, MysteriousCapModel::getTexturedModelData);

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0
						? WahooRegistry.MYSTERIOUS_CAP.getColor(stack)
						: 0xFFFFFF,
				WahooRegistry.MYSTERIOUS_CAP
		);

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
}
