package net.ignoramuses.bingBingWahoo.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("xRot")
	void setXRotRaw(float pitch);
	
	@Accessor("yRot")
	void setYRotRaw(float yaw);
}
