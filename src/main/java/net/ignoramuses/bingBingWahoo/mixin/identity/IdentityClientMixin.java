package net.ignoramuses.bingBingWahoo.mixin.identity;

import draylar.identity.IdentityClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.ignoramuses.bingBingWahoo.BingBingWahooClient;
import net.ignoramuses.bingBingWahoo.WahooCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Environment(EnvType.CLIENT)
@Mixin(IdentityClient.class)
public abstract class IdentityClientMixin {
	@ModifyArgs(method = "onInitializeClient", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V", remap = false), remap = false)
	private void wahoo$onIdentityStartTick(Args args) {
		StartTick listener = args.get(0);
		StartTick wahooWrapper = client -> {
			Object o = BingBingWahooClient.GAME_RULES.get(WahooCommands.DISABLE_IDENTITY_SWAPPING_RULE.getName());
			if (o instanceof Boolean disabled && !disabled) {
				listener.onStartTick(client);
			}
		};
		args.set(0, wahooWrapper);
	}
}
