package net.ignoramuses.bingBingWahoo.cap;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ignoramuses.bingBingWahoo.BingBingWahoo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;
import static net.ignoramuses.bingBingWahoo.WahooNetworking.CAP_ENTITY_SPAWN;
import static net.minecraft.world.entity.Entity.RemovalReason.*;

public class FlyingCapEntity extends Entity implements ItemSupplier {
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(FlyingCapEntity.class, EntityDataSerializers.INT);
	
	// synced
	private ItemStack stack;
	private String throwerId;
	private Vec3 startAngle;
	private Vec3 startPos;
	private boolean leftThrower = false;
	public int ticksAtEnd;
	private PreferredCapSlot preferredSlot;
	// not synced
	@Nullable
	private Player thrower;
	
	public FlyingCapEntity(EntityType<FlyingCapEntity> entityType, Level world) {
		super(entityType, world);
	}
	
	public FlyingCapEntity(Level world, ItemStack itemStack, Player thrower, double x, double y, double z, PreferredCapSlot slot) {
		super(BingBingWahoo.FLYING_CAP, world);
		this.setItem(itemStack.copy());
		this.thrower = thrower;
		this.throwerId = thrower.getStringUUID();
		this.startAngle = thrower.getViewVector(0).normalize().scale(0.1);
		setPosRaw(x, y, z);
		this.startPos = position();
		this.preferredSlot = slot;
	}
	
	public void sendData(ServerPlayer player) {
		CompoundTag data = new CompoundTag();
		addAdditionalSaveData(data);
		FriendlyByteBuf buf = PacketByteBufs.create()
				.writeNbt(data)
				.writeUtf(getStringUUID());
		ServerPlayNetworking.send(player, CAP_ENTITY_SPAWN, buf);
	}
	
	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		sendData(player);
	}
	
	@Override
	protected void defineSynchedData() {
		entityData.define(COLOR, 0xFFFFFF);
	}
	
	public void setItem(ItemStack stack) {
		this.stack = stack;
		if (stack.getItem() instanceof MysteriousCapItem cap) {
			setColor(cap.getColor(stack));
		}
	}
	
	public void setColor(int color) {
		entityData.set(COLOR, color);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		ItemStack stack = ItemStack.of(nbt.getCompound("Item"));
		setItem(stack);
		this.throwerId = nbt.getString("Thrower");
		this.startAngle = new Vec3(nbt.getDouble("StartAngleX"), nbt.getDouble("StartAngleY"), nbt.getDouble("StartAngleZ"));
		this.startPos = new Vec3(nbt.getDouble("StartPosX"), nbt.getDouble("StartPosY"), nbt.getDouble("StartPosZ"));
		this.leftThrower = nbt.getBoolean("LeftThrower");
		this.ticksAtEnd = nbt.getInt("TicksAtEnd");
		this.preferredSlot = PreferredCapSlot.values()[nbt.getInt("PreferredSlot")];
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		nbt.put("Item", getItem().save(new CompoundTag()));
		nbt.putString("Thrower", throwerId);
		nbt.putDouble("StartAngleX", startAngle.x());
		nbt.putDouble("StartAngleY", startAngle.y());
		nbt.putDouble("StartAngleZ", startAngle.z());
		nbt.putDouble("StartPosX", startPos.x());
		nbt.putDouble("StartPosY", startPos.y());
		nbt.putDouble("StartPosZ", startPos.z());
		nbt.putBoolean("LeftThrower", leftThrower);
		nbt.putInt("TicksAtEnd", ticksAtEnd);
		nbt.putInt("PreferredSlot", preferredSlot.ordinal());
	}
	
	@Override
	public void tick() {
		super.tick();
		tryFindThrower();
		tryMove();
		
		if (!level.isClientSide()) {
			if (tickCount == 1 || tickCount % 40 == 0) // todo
				level.playSound(null, this, SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.5f, 2);
			
			List<Entity> collisions = level.getEntities(this, getBoundingBox().expandTowards(getDeltaMovement()).inflate(1), e -> !e.isSpectator() && e.isPickable());
			
			if (shouldLeaveThrower(collisions)) {
				leftThrower = true;
			}
			
			for (Entity entity : collisions) {
				if (entity instanceof Player thrower) {
					if ((leftThrower || ticksAtEnd != 0) && throwerId.equals(entity.getStringUUID())) {
						if (!tryReequipCap()) { // set in correct slot
							if (!thrower.addItem(getItem())) { // throw randomly in inventory
								level.addFreshEntity(new ItemEntity(level, thrower.getX(), thrower.getY(), thrower.getZ(), getItem())); // drop on ground
							}
						}
						remove(KILLED); // bypass item drop
						level.playSound(null,
								thrower.getX(),
								thrower.getY(),
								thrower.getZ(),
								SoundEvents.ITEM_PICKUP,
								SoundSource.PLAYERS,
								0.2F,
								((thrower.getRandom().nextFloat() - thrower.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
						);
					}
				}
				
				
				if (entity != thrower && entity instanceof LivingEntity living) {
					living.hurt(DamageSource.thrown(this, thrower), 3);
					ticksAtEnd = 10;
					break;
				}
			}
			
			if (tickCount > 500) kill();
		}
	}
	
	private boolean tryReequipCap() {
		if (thrower != null) {
			ItemStack stack = getItem();
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
			for (Player player : level.players()) {
				if (player.getStringUUID().equals(throwerId)) {
					thrower = player;
				}
			}
		}
	}
	
	private void tryMove() {
		if (thrower != null) {
			Vec3 toMove = Vec3.ZERO;
			if (ticksAtEnd == 0) {
				double mult = Math.cos(tickCount / 8f) * 10;
				toMove = startAngle.scale(mult);
				// valley of the cosine wave - slow down as it approaches the end
				ticksAtEnd = mult <= 0 ? 1 : 0;
			} else {
				ticksAtEnd++;
				if (ticksAtEnd > 10) {
					Vec3 distance = thrower.getEyePosition().subtract(position());
					toMove = distance.scale(0.2);
				}
			}
			
			if (toMove != Vec3.ZERO) {
				move(MoverType.SELF, toMove);
			}
		}
	}
	
	private boolean shouldLeaveThrower(List<Entity> collisions) {
		for(Entity entity : collisions) {
			if (entity.getStringUUID().equals(throwerId)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isPickable() {
		return true;
	}
	
	@Override
	public boolean canChangeDimensions() {
		return false;
	}
	
	@Override
	public void kill() {
		super.kill();
		spawnAtLocation(stack.copy());
	}

	@Override
	public ItemStack getItem() {
		return stack;
	}
	
	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
	
	public static void spawn(ServerPlayer thrower, ItemStack capStack, PreferredCapSlot preferredSlot) {
		if (capStack != null && capStack.is(MYSTERIOUS_CAP)) {
			FlyingCapEntity cap = new FlyingCapEntity(thrower.level, capStack.copy(), thrower, thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ(), preferredSlot);
			thrower.level.addFreshEntity(cap);
		}
	}
}
