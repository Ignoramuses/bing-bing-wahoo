package net.ignoramuses.bingBingWahoo.cap;

import draylar.identity.Identity;
import draylar.identity.registry.Components;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.ignoramuses.bingBingWahoo.WahooNetworking;
import net.ignoramuses.bingBingWahoo.WahooUtils.ServerPlayerEntityExtensions;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;
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
	public int ticksAtEnd;
	private PreferredCapSlot preferredSlot;
	// not synced
	@Nullable
	private PlayerEntity thrower;
	
	public FlyingCapEntity(EntityType<FlyingCapEntity> entityType, World world) {
		super(entityType, world);
	}
	
	public FlyingCapEntity(World world, ItemStack itemStack, PlayerEntity thrower, double x, double y, double z, PreferredCapSlot slot) {
		super(BingBingWahoo.FLYING_CAP, world);
		this.setItem(itemStack.copy());
		this.thrower = thrower;
		this.throwerId = thrower.getUuidAsString();
		this.startAngle = thrower.getRotationVec(0).normalize().multiply(0.1);
		setPos(x, y, z);
		this.startPos = getPos();
		this.preferredSlot = slot;
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
	
	public void setItem(ItemStack stack) {
		this.stack = stack;
		if (stack.getItem() instanceof MysteriousCapItem cap) {
			setColor(cap.getColor(stack));
		}
	}
	
	public void setColor(int color) {
		dataTracker.set(COLOR, color);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		ItemStack stack = ItemStack.fromNbt(nbt.getCompound("Item"));
		setItem(stack);
		this.throwerId = nbt.getString("Thrower");
		this.startAngle = new Vec3d(nbt.getDouble("StartAngleX"), nbt.getDouble("StartAngleY"), nbt.getDouble("StartAngleZ"));
		this.startPos = new Vec3d(nbt.getDouble("StartPosX"), nbt.getDouble("StartPosY"), nbt.getDouble("StartPosZ"));
		this.leftThrower = nbt.getBoolean("LeftThrower");
		this.ticksAtEnd = nbt.getInt("TicksAtEnd");
		this.preferredSlot = PreferredCapSlot.values()[nbt.getInt("PreferredSlot")];
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.put("Item", getStack().writeNbt(new NbtCompound()));
		nbt.putString("Thrower", throwerId);
		nbt.putDouble("StartAngleX", startAngle.getX());
		nbt.putDouble("StartAngleY", startAngle.getY());
		nbt.putDouble("StartAngleZ", startAngle.getZ());
		nbt.putDouble("StartPosX", startPos.getX());
		nbt.putDouble("StartPosY", startPos.getY());
		nbt.putDouble("StartPosZ", startPos.getZ());
		nbt.putBoolean("LeftThrower", leftThrower);
		nbt.putInt("TicksAtEnd", ticksAtEnd);
		nbt.putInt("PreferredSlot", preferredSlot.ordinal());
	}
	
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
						if (!tryReequipCap()) { // set in correct slot
							if (!thrower.giveItemStack(getStack())) { // throw randomly in inventory
								world.spawnEntity(new ItemEntity(world, thrower.getX(), thrower.getY(), thrower.getZ(), getStack())); // drop on ground
							}
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
				}
				
				
				if (entity instanceof LivingEntity living && !(entity instanceof PlayerEntity) && thrower instanceof ServerPlayerEntity player) {
					if (Identity.CONFIG.enableSwaps || player.hasPermissionLevel(3)) {
						LivingEntity copy = (LivingEntity) living.getType().create(world);
						if (copy != null) {
							NbtCompound captured = new NbtCompound();
							NbtCompound entityData = living.writeNbt(new NbtCompound());
							entityData.remove("Pos");
							entityData.remove("Motion");
							entityData.remove("Rotation");
							captured.put("Entity", entityData);
							captured.putString("Type", Registry.ENTITY_TYPE.getId(living.getType()).toString());
							copy.readNbt(entityData);
							Components.CURRENT_IDENTITY.get(player).setIdentity(copy);
							((ServerPlayerEntityExtensions) player).wahoo$setCaptured(captured);
							ServerPlayNetworking.send(player, WahooNetworking.CAPTURE, PacketByteBufs.create().writeNbt(entityData));
							player.calculateDimensions();
							player.teleport((ServerWorld) world, living.getX(), living.getY(), living.getZ(), living.getYaw(), living.getPitch());
							world.playSound(null, living.getX(), living.getY(), living.getZ(),
									SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
							living.discard();
							ItemStack stack = getStack();
							if (!tryReequipCap()) { // set in correct slot
								if (!player.giveItemStack(stack)) { // throw randomly in inventory
									world.spawnEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), stack)); // drop on ground
								}
							}
							remove(KILLED);
							break;
						}
					}
				} else if (entity != thrower && entity instanceof LivingEntity living) {
					living.damage(DamageSource.thrownProjectile(this, thrower), 3);
					ticksAtEnd = 10;
					break;
				}
			}
			
			if (age > 500) kill();
		}
	}
	
	private boolean tryReequipCap() {
		if (thrower != null) {
			ItemStack stack = getStack();
			// first try preferred slot
			if (preferredSlot.shouldEquip(thrower, stack)) {
				preferredSlot.equip(thrower, stack);
				return true;
			}
			
			// then try all slots
			for (PreferredCapSlot slot : PreferredCapSlot.values()) {
				if (slot.shouldEquip(thrower, stack)) {
					slot.shouldEquip(thrower, stack);
					return true;
				}
			}
			
		}
		return false;
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
	
	public static void spawn(ServerPlayerEntity thrower, ItemStack capStack, PreferredCapSlot preferredSlot) {
		if (capStack != null && capStack.isOf(MYSTERIOUS_CAP)) {
			FlyingCapEntity cap = new FlyingCapEntity(thrower.world, capStack.copy(), thrower, thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ(), preferredSlot);
			thrower.world.spawnEntity(cap);
		}
	}
}
