package net.ignoramuses.bingBingWahoo.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(ModelPart.class)
public interface ModelPartAccessor {
	@Accessor("children")
	Map<String, ModelPart> getChildren();
}
