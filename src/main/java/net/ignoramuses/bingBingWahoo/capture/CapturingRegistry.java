package net.ignoramuses.bingBingWahoo.capture;

import net.ignoramuses.bingBingWahoo.capture.builtin.PigCaptureHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class CapturingRegistry {
	private static final Map<EntityType<? extends Entity>, CaptureHandler<? extends Entity>> HANDLERS = new HashMap<>();
	
	public static void register(EntityType<?> type, CaptureHandler<?> handler) {
		HANDLERS.put(type, handler);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> CaptureHandler<T> get(EntityType<T> type) {
		return (CaptureHandler<T>) HANDLERS.get(type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> CaptureHandler<T> get(T entity) {
		return (CaptureHandler<T>) get(entity.getType());
	}
	
	public static void registerDefaults() {
		register(EntityType.PIG, PigCaptureHandler.INSTANCE);
	}
}
