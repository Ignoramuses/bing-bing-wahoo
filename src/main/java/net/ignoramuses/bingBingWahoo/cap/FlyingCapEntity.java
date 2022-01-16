package net.ignoramuses.bingBingWahoo.cap;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.capture.CaptureHandler;
import net.ignoramuses.bingBingWahoo.capture.CapturingRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.ignoramuses.bingBingWahoo.WahooNetworking.CAP_ENTITY_SPAWN;
import static net.minecraft.entity.Entity.RemovalReason.*;

public class FlyingCapEntity extends Entity implements FlyingItemEntity {
	private static final TrackedData<Integer> COLOR = DataTracker.registerData(FlyingCapEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	// synced
	private ItemStack stack;
	private String throwerId;
	private Vec3d startAngle;
	private Vec3d startPos;
	private boolean leftThrower = false;
	private int ticksAtEnd;
	// not synced
	@Nullable
	private PlayerEntity thrower;
	
	// region init
	
	public FlyingCapEntity(EntityType<FlyingCapEntity> entityType, World world) {
		super(entityType, world);
	}
	
	public FlyingCapEntity(World world, ItemStack itemStack, PlayerEntity thrower, double x, double y, double z) {
		super(BingBingWahoo.FLYING_CAP, world);
		this.setItem(itemStack.copy());
		this.thrower = thrower;
		this.throwerId = thrower.getUuidAsString();
		startAngle = thrower.getRotationVec(0).normalize().multiply(0.1);
		setPos(x, y, z);
		startPos = getPos();
	}
	
	public void sendData(ServerPlayerEntity player) {
		NbtCompound data = new NbtCompound();
		writeCustomDataToNbt(data);
		PacketByteBuf buf = PacketByteBufs.create()
				.writeNbt(data)
				.writeString(getUuidAsString());
		ServerPlayNetworking.send(player, CAP_ENTITY_SPAWN, buf);
	}
	
	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		sendData(player);
	}
	
	@Override
	protected void initDataTracker() {
		dataTracker.startTracking(COLOR, 0xFFFFFF);
	}
	
	// endregion
	// region setters
	
	public void setItem(ItemStack stack) {
		this.stack = stack;
		if (stack.getItem() instanceof MysteriousCapItem cap) {
			setColor(cap.getColor(stack));
		}
	}
	
	public void setColor(int color) {
		dataTracker.set(COLOR, color);
	}
	
	// endregion
	// region data
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		ItemStack stack = ItemStack.fromNbt(nbt.getCompound("Item"));
		setItem(stack);
		this.throwerId = nbt.getString("Thrower");
		this.startAngle = new Vec3d(nbt.getDouble("StartAngleX"), nbt.getDouble("StartAngleY"), nbt.getDouble("StartAngleZ"));
		this.startPos = new Vec3d(nbt.getDouble("StartPosX"), nbt.getDouble("StartPosY"), nbt.getDouble("StartPosZ"));
		this.leftThrower = nbt.getBoolean("LeftThrower");
		this.ticksAtEnd = nbt.getInt("TicksAtEnd");
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		ItemStack stack = getStack();
		if (stack != null) nbt.put("Item", stack.writeNbt(new NbtCompound()));
		nbt.putString("Thrower", throwerId);
		nbt.putDouble("StartAngleX", startAngle.getX());
		nbt.putDouble("StartAngleY", startAngle.getY());
		nbt.putDouble("StartAngleZ", startAngle.getZ());
		nbt.putDouble("StartPosX", startPos.getX());
		nbt.putDouble("StartPosY", startPos.getY());
		nbt.putDouble("StartPosZ", startPos.getZ());
		nbt.putBoolean("LeftThrower", leftThrower);
		nbt.putInt("TicksAtEnd", ticksAtEnd);
	}
	
	// endregion
	
	@Override
	public void tick() {
		super.tick();
		tryFindThrower();
		tryMove();
		
		if (!world.isClient()) {
			List<Entity> collisions = world.getOtherEntities(this, getBoundingBox().stretch(getVelocity()).expand(1), e -> !e.isSpectator() && e.collides());
			
			if (shouldLeaveThrower(collisions)) {
				leftThrower = true;
			}
			
			for (Entity entity : collisions) {
				if (entity instanceof PlayerEntity thrower) {
					if ((leftThrower || ticksAtEnd != 0) && throwerId.equals(entity.getUuidAsString())) {
						if (thrower.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
							thrower.setStackInHand(Hand.MAIN_HAND, getStack());
						} else {
							thrower.giveItemStack(getStack());
						}
						remove(KILLED);
						world.playSound(null,
								thrower.getX(),
								thrower.getY(),
								thrower.getZ(),
								SoundEvents.ENTITY_ITEM_PICKUP,
								SoundCategory.PLAYERS,
								0.2F,
								((thrower.getRandom().nextFloat() - thrower.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
						);
					}
					
					CaptureHandler<?> captureHandler = CapturingRegistry.get(entity);
					if (captureHandler != null) {
						// todo capturing
					}
				}
			}
			
			if (age > 500) kill();
		}
	}
	
	private void tryFindThrower() {
		if (thrower == null) {
			for (PlayerEntity player : world.getPlayers()) {
				if (player.getUuidAsString().equals(throwerId)) {
					thrower = player;
				}
			}
		}
	}
	
	private void tryMove() {
		if (thrower != null) {
			Vec3d toMove = Vec3d.ZERO;
			if (ticksAtEnd == 0) {
				double mult = Math.cos(age / 5f) * 10;
				toMove = startAngle.multiply(mult);
				// valley of the cosine wave - slow down as it approaches the end
				ticksAtEnd = mult <= 0 ? 1 : 0;
			} else {
				ticksAtEnd++;
				if (ticksAtEnd > 10) {
					if (thrower != null) {
						Vec3d distance = thrower.getEyePos().subtract(getPos());
						toMove = distance.multiply(0.2);
					}
				}
			}
			
			if (toMove != Vec3d.ZERO) {
				move(MovementType.SELF, toMove);
			}
		}
	}
	
	private boolean shouldLeaveThrower(List<Entity> collisions) {
		for(Entity entity : collisions) {
			if (entity.getUuidAsString().equals(throwerId)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean collides() {
		return true;
	}
	
	@Override
	public boolean canUsePortals() {
		return false;
	}
	
	@Override
	public void kill() {
		super.kill();
		dropStack(stack.copy());
	}

	@Override
	public ItemStack getStack() {
		return stack;
	}
	
	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}
}
