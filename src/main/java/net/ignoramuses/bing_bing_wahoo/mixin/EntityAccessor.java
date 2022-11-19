package net.ignoramuses.bing_bing_wahoo.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("xRot")
	void wahoo$setXRotRaw(float pitch);

	@Invoker("calculateViewVector")
	Vec3 wahoo$calculateViewVector(float pitch, float yaw);
}
