package net.ignoramuses.bingBingWahoo.mixin.modelAccessors;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer.DragonEntityModel;
import net.minecraft.client.render.entity.model.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({AxolotlEntityModel.class, BatEntityModel.class, OcelotEntityModel.class,
		ChickenEntityModel.class, QuadrupedEntityModel.class, CreeperEntityModel.class,
		HorseEntityModel.class, DragonEntityModel.class, FoxEntityModel.class,
		GuardianEntityModel.class, HoglinEntityModel.class, IronGolemEntityModel.class,
		LlamaEntityModel.class, ParrotEntityModel.class, RabbitEntityModel.class,
		RavagerEntityModel.class, SnowGolemEntityModel.class, SpiderEntityModel.class, WolfEntityModel.class})
public interface EntityModelWithHeadAccessor {
	@Accessor("head")
	ModelPart wahoo$getHead();
}
