package net.ignoramuses.bingBingWahoo.mixin.identity;

import draylar.identity.command.IdentityCommand;
import net.ignoramuses.bingBingWahoo.WahooCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IdentityCommand.class)
public abstract class IdentityCommandMixin {
	@Inject(method = "equip", at = @At("HEAD"), cancellable = true, remap = false)
	private static void wahoo$equip(ServerPlayerEntity source, ServerPlayerEntity player, Identifier identity, CallbackInfo ci) {
		if (player.getWorld().getGameRules().getBoolean(WahooCommands.DISABLE_IDENTITY_SWAPPING_RULE)) {
			source.sendMessage(new TranslatableText("bingbingwahoo.identity_disabled"), false);
			ci.cancel();
		}
	}
	
	@Inject(method = "unequip", at = @At("HEAD"), cancellable = true, remap = false)
	private static void wahoo$unequip(ServerPlayerEntity source, ServerPlayerEntity player, CallbackInfo ci) {
		if (player.getWorld().getGameRules().getBoolean(WahooCommands.DISABLE_IDENTITY_SWAPPING_RULE)) {
			source.sendMessage(new TranslatableText("bingbingwahoo.identity_disabled"), false);
			ci.cancel();
		}
	}
}
