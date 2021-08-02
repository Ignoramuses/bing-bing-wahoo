package net.ignoramuses.bingBingWahoo.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("pitch")
	void setPitchRaw(float pitch);
	
	@Accessor("yaw")
	void setYawRaw(float yaw);
}
