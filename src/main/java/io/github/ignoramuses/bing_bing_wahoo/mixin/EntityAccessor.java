package io.github.ignoramuses.bing_bing_wahoo.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("xRot")
	void setXRotRaw(float pitch);

	@Invoker
	Vec3 callCalculateViewVector(float pitch, float yaw);
}
