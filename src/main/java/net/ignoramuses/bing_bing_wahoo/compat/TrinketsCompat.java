package net.ignoramuses.bing_bing_wahoo.compat;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.ignoramuses.bing_bing_wahoo.WahooRegistry.MYSTERIOUS_CAP;

public class TrinketsCompat {
	public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

	public static boolean capTrinketEquipped(Player player) {
		if (!TRINKETS_LOADED) {
			return false;
		}
		Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
		return component.map(trinketComponent -> trinketComponent.isEquipped(MYSTERIOUS_CAP)).orElse(false);
	}
	
	@Nullable
	public static ItemStack getCapTrinketStack(LivingEntity entity) {
		if (!TRINKETS_LOADED) {
			return null;
		}
		Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
		if (component.isPresent()) {
			for (Tuple<SlotReference, ItemStack> pair : component.get().getEquipped(MYSTERIOUS_CAP)) {
				return pair.getB();
			}
		}
		return null;
	}
	
	public static void equipInHatTrinketSlot(LivingEntity entity, ItemStack stack) {
		if (!TRINKETS_LOADED) {
			return;
		}
		Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
		if (component.isPresent()) {
			for (Tuple<SlotReference, ItemStack> pair : component.get().getEquipped(item -> true)) {
				SlotReference slot = pair.getA();
				TrinketInventory inv = slot.inventory();
				if (inv.getSlotType().getName().equals("hat")) {
					inv.setItem(slot.index(), stack);
				}
			}
		}
	}
}
