package io.github.ignoramuses.bing_bing_wahoo.content.cap;

import io.github.ignoramuses.bing_bing_wahoo.packets.CapSpawnPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.github.ignoramuses.bing_bing_wahoo.WahooNetworking;
import io.github.ignoramuses.bing_bing_wahoo.WahooRegistry;
import io.github.ignoramuses.bing_bing_wahoo.WahooUtils;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.ignoramuses.bing_bing_wahoo.WahooRegistry.MYSTERIOUS_CAP;
import static net.minecraft.world.entity.Entity.RemovalReason.*;

public class FlyingCapEntity extends Entity implements ItemSupplier {
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(FlyingCapEntity.class, EntityDataSerializers.INT);

	// synced
	private ItemStack stack;
	private UUID throwerId;
	private Vec3 startAngle;
	private Vec3 startPos;
	public int ticksAtEnd;
	private PreferredCapSlot preferredSlot;
	// not synced
	@Nullable
	private Player thrower;
	private final List<Entity> carriedEntities = new ArrayList<>();
	private boolean leftThrower = false;
	@Environment(EnvType.CLIENT)
	private CapFlyingSoundInstance whooshSound;
	
	public FlyingCapEntity(EntityType<FlyingCapEntity> entityType, Level world) {
		super(entityType, world);
	}
	
	public FlyingCapEntity(Level world, ItemStack itemStack, Player thrower, double x, double y, double z, PreferredCapSlot slot) {
		super(WahooRegistry.FLYING_CAP, world);
		this.setItem(itemStack.copy());
		this.thrower = thrower;
		this.throwerId = thrower.getGameProfile().getId();
		this.startAngle = adjustStartAngle(thrower.getViewVector(0).normalize().scale(0.1));
		setPosRaw(x, y, z);
		this.startPos = position();
		this.preferredSlot = slot;
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
		this.throwerId = nbt.getUUID("Thrower");
		this.startAngle = new Vec3(nbt.getDouble("StartAngleX"), nbt.getDouble("StartAngleY"), nbt.getDouble("StartAngleZ"));
		this.startPos = new Vec3(nbt.getDouble("StartPosX"), nbt.getDouble("StartPosY"), nbt.getDouble("StartPosZ"));
		this.leftThrower = nbt.getBoolean("LeftThrower");
		this.ticksAtEnd = nbt.getInt("TicksAtEnd");
		this.preferredSlot = PreferredCapSlot.values()[nbt.getInt("PreferredSlot")];
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		nbt.put("Item", getItem().save(new CompoundTag()));
		nbt.putUUID("Thrower", throwerId);
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
		moveCarriedEntities();
		handleCollisions();

		if (level.isClientSide()) {
			playWhoosh();
		} else {
			if (tickCount > 500) kill();
		}
	}

	private void handleCollisions() {
		List<Entity> collisions = level.getEntities(
				this,
				getBoundingBox().expandTowards(getDeltaMovement()).inflate(1),
				e -> !e.isSpectator()
		);

		if (shouldLeaveThrower(collisions)) {
			leftThrower = true;
		}

		for (Entity entity : collisions) {
			if (thrower != null) {
				boolean client = level.isClientSide();
				if (entity == thrower) {
					if (!client && (leftThrower || ticksAtEnd != 0)) {
						giveThrowerItem();
						dropCarried();
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
				} else {
					if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
						carriedEntities.add(entity);
					} else if (entity instanceof LivingEntity living && !client) {
						living.hurt(DamageSource.thrown(this, thrower), 3);
						ticksAtEnd = 10;
					}
				}
			}
		}
	}

	private void giveThrowerItem() {
		if (thrower != null) {
			if (!tryReequipCap()) { // set in correct slot
				thrower.getInventory().placeItemBackInInventory(getItem()); // throw in inv or on ground if no space
			}
		}
	}

	private void dropCarried() {
		if (thrower != null && !carriedEntities.isEmpty()) {
			Inventory inv = thrower.getInventory();
			for (Entity entity : carriedEntities) {
				if (entity.isRemoved()) continue;
				if (entity instanceof ItemEntity item) {
					ItemStack stack = item.getItem();
					inv.placeItemBackInInventory(stack);
					item.discard();
				}
			}
		}
	}

	private void moveCarriedEntities() {
		if (!carriedEntities.isEmpty()) {
			Vec3 pos = position().add(0, 0.4, 0);
			for (ListIterator<Entity> itr = carriedEntities.listIterator(); itr.hasNext();) {
				Entity carried = itr.next();
				if (carried.isRemoved()) {
					itr.remove();
					continue;
				}
				carried.setPos(pos);
				carried.resetFallDistance();
			}
		}
	}
	
	private void tryFindThrower() {
		if (thrower == null) {
			for (Player player : level.players()) {
				if (player.getGameProfile().getId().equals(throwerId)) {
					thrower = player;
				}
			}
		}
	}
	
	private void tryMove() {
		Vec3 toMove = Vec3.ZERO;
		if (ticksAtEnd == 0) {
			double mult = Math.cos(tickCount / 8f) * 10;
			toMove = startAngle.scale(mult);
			// valley of the cosine wave - slow down as it approaches the end
			ticksAtEnd = mult <= 0 ? 1 : 0;
		} else if (thrower != null) {
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

	private boolean shouldLeaveThrower(List<Entity> collisions) {
		for (Entity entity : collisions) {
			if (entity == thrower) {
				return false;
			}
		}
		return true;
	}

	private boolean tryReequipCap() {
		if (thrower != null) {
			ItemStack stack = getItem();
			if (preferredSlot.shouldEquip(thrower, stack)) {
				preferredSlot.equip(thrower, stack);
				return true;
			}
		}
		return false;
	}

	@Environment(EnvType.CLIENT)
	private void playWhoosh() {
		if (whooshSound == null) {
			Minecraft mc = Minecraft.getInstance();
			whooshSound = new CapFlyingSoundInstance(this);
			mc.getSoundManager().play(whooshSound);
		}
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
	public boolean isPickable() {
		return super.isPickable();
	}

	@Override
	public ItemStack getItem() {
		return stack;
	}
	
	@Override
	public Packet<?> getAddEntityPacket() {
		return CapSpawnPacket.makePacket(this);
	}

	public RandomSource getRandom() {
		return this.random;
	}

	private static Vec3 adjustStartAngle(Vec3 startAngle) {
		// x and z near zero, but not y
		// you don't even want to know why this is needed.
		// you want to know anyway? fine.
		// somehow, only on dedicated servers, only when looking straight down with
		// sub-pixel accuracy, 8 ghost-voxelshapes prevented the cap from moving down.
		// I don't know how. I don't want to know how. All I know is that this slight
		// offset is somehow able to fix it.
		// Nearly a full day was spent figuring this out, I give up. You win, MC.
		if (WahooUtils.aprox(startAngle.x, 0, 0.001)) {
			if (WahooUtils.aprox(startAngle.z, 0, 0.001)) {
				if (!WahooUtils.aprox(startAngle.y, 0, 0.001)) {
					return new Vec3(0.001, startAngle.y, 0.001);
				}
			}
		}
		return startAngle;
	}

	public static void spawn(ServerPlayer thrower, ItemStack capStack, PreferredCapSlot preferredSlot) {
		ItemCooldowns cooldowns = thrower.getCooldowns();
		if (capStack != null && capStack.is(MYSTERIOUS_CAP) && !cooldowns.isOnCooldown(MYSTERIOUS_CAP)) {
			FlyingCapEntity cap = new FlyingCapEntity(thrower.level, capStack.copy(), thrower, thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ(), preferredSlot);
			thrower.level.addFreshEntity(cap);
			cooldowns.addCooldown(MYSTERIOUS_CAP, 20);
			thrower.swing(InteractionHand.MAIN_HAND, true);
			preferredSlot.equip(thrower, ItemStack.EMPTY);
		}
	}
}
