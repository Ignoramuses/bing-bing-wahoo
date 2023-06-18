package io.github.ignoramuses.bing_bing_wahoo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Predicate;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;

@Mixin(EntitySelectorOptions.class)
public interface EntitySelectorOptionsAccessor {
	@Invoker
	static void callRegister(String id, EntitySelectorOptions.Modifier handler,
							 Predicate<EntitySelectorParser> condition, Component description) {
		throw new RuntimeException("mixin failed!");
	}
}
