package net.ignoramuses.bingBingWahoo.mixin.identity;

import draylar.identity.IdentityClient;
import draylar.identity.screen.IdentityScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient;
import net.ignoramuses.bingBingWahoo.WahooCommands;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class IdentityMinecraftClientMixin {
	@Shadow @Nullable public ClientPlayerEntity player;
	
	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void wahoo$preventOpeningIdentityScreen(Screen screen, CallbackInfo ci) {
		if (screen instanceof IdentityScreen) {
			Object o = BingBingWahooClient.GAME_RULES.get(WahooCommands.DISABLE_IDENTITY_SWAPPING_RULE.getName());
			if (o instanceof Boolean disabled && disabled) {
				player.sendMessage(new TranslatableText("bingbingwahoo.identity_disabled"), false);
				ci.cancel();
			}
		}
	}
}
