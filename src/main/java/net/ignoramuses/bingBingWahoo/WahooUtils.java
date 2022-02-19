package net.ignoramuses.bingBingWahoo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ignoramuses.bingBingWahoo.cap.MysteriousCapModel;
import net.ignoramuses.bingBingWahoo.mixin.EntityModelWithHeadAccessor;
import net.ignoramuses.bingBingWahoo.movement.JumpTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.ignoramuses.bingBingWahoo.BingBingWahoo.MYSTERIOUS_CAP;

public class WahooUtils {
	public static final double SIXTEENTH = 1 / 16f;
	public static final Identifier CAP_TEXTURE = new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap.png");
	public static final Identifier EMBLEM_TEXTURE = new Identifier(BingBingWahoo.ID, "textures/armor/mysterious_cap_emblem.png");
	
	public static NbtList toNbtList(double... values) {
		NbtList nbtList = new NbtList();
		
		for(double d : values) {
			nbtList.add(NbtDouble.of(d));
		}
		
		return nbtList;
	}
	
	@Environment(EnvType.CLIENT)
	public static void renderCap(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack hatStack, int light, float tickDelta, MysteriousCapModel model) {
		int color = MYSTERIOUS_CAP.getColor(hatStack);
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
		VertexConsumer capConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(CAP_TEXTURE), false, hatStack.hasGlint());
		model.render(matrices, capConsumer, light, 1, r, g, b, 1);
		VertexConsumer emblemConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(EMBLEM_TEXTURE), false, hatStack.hasGlint());
		model.render(matrices, emblemConsumer, light, 1, 1, 1, 1, 1);
		matrices.pop();
	}
	
	@Nullable
	@Environment(EnvType.CLIENT)
	public static ModelPart getHeadModel(@Nullable Model base) {
		if (base instanceof BipedEntityModel biped) {
			return biped.head;
		} else if (base instanceof ModelWithHead model) {
			return model.getHead();
		} else if (base instanceof EntityModelWithHeadAccessor model) {
			return model.wahoo$getHead();
		}
		
		return null;
	}
	
	public static double getVelocityForSlopeDirection(Direction directionOfSlope) {
		return switch (directionOfSlope) {
			case NORTH, WEST -> 0.1;
			case SOUTH, EAST -> -0.1;
			default -> throw new IllegalStateException("Unexpected value: " + directionOfSlope);
		};
	}
	
	public static double getVelocityForSlidingOnGround(Direction direction) {
		return switch (direction) {
			case NORTH, WEST -> -0.035;
			case SOUTH, EAST -> 0.035;
			default -> throw new IllegalStateException("Unexpected value: " + direction);
		};
	}
	
	public static double capWithSign(double value, double max) {
		double valueAbs = Math.abs(value);
		double newValue = Math.min(valueAbs, max);
		return Math.copySign(newValue, value);
	}
	
	public static Direction getHorizontalDirectionFromVector(Vec3d vector) {
		double x = vector.getX();
		double z = vector.getZ();
		
		if (Math.abs(x) > Math.abs(z)) {
			if (x > 0) {
				return Direction.EAST;
			}
			return Direction.WEST;
		} else {
			if (z > 0) {
				return Direction.SOUTH;
			}
			return Direction.NORTH;
		}
	}
	
	/**
	 * Checks both if a BlockState is a slope and is facing in a compatible direction.
	 * @param state BlockState to check
	 * @return If the BlockState can be slid up
	 */
	public static boolean canGoUpSlope(BlockState state, Direction playerMoving) {
		if (state.getBlock() instanceof StairsBlock) {
			if (state.get(Properties.BLOCK_HALF).equals(BlockHalf.TOP)) return false;
			Direction blockFacing = state.get(HorizontalFacingBlock.FACING);
			StairShape shape = state.get(Properties.STAIR_SHAPE);
			Direction secondaryDirection = switch (shape) {
				case INNER_LEFT, OUTER_LEFT, STRAIGHT -> blockFacing.rotateYCounterclockwise();
				case INNER_RIGHT, OUTER_RIGHT -> blockFacing.rotateYClockwise();
			};
			Direction tertiaryDirection = shape == StairShape.STRAIGHT ? secondaryDirection.getOpposite() : null;
			return playerMoving == blockFacing || playerMoving == secondaryDirection || playerMoving == tertiaryDirection;
		} else if (blockIsAutomobilitySlope(state)) {
			Direction blockFacing = state.get(HorizontalFacingBlock.FACING).getOpposite();
			Direction left = blockFacing.rotateYCounterclockwise();
			Direction right = blockFacing.rotateYClockwise();
			return blockFacing == playerMoving || left == playerMoving || right == playerMoving;
		}
		return false;
	}
	
	public static boolean blockIsSlope(BlockState state) {
		return state.getBlock() instanceof StairsBlock || blockIsAutomobilitySlope(state);
	}
	
	public static boolean blockIsAutomobilitySlope(BlockState state) {
		return blockIsAutomobilitySlope(state.getBlock());
	}
	
	public static boolean blockIsAutomobilitySlope(Block block) {
		String className = block.getClass().getName();
		return className.equals("io.github.foundationgames.automobility.block.SlopeBlock") ||
				className.equals("io.github.foundationgames.automobility.block.SlopedDashPanelBlock") ||
				className.equals("io.github.foundationgames.automobility.block.SteepSlopeBlock") ||
				className.equals("io.github.foundationgames.automobility.block.SteepSlopedDashPanelBlock");
	}
	
	/**
	 * Whether a number is close to another number or not.
	 * @param number The number to compare
	 * @param target The number to compare against
	 * @param range The range in which the numbers are approximately the same, inclusive
	 * @return Whether the difference between number and target is less than or equal to range.
	 */
	public static boolean aprox(double number, double target, double range) {
		if (number == target) return true;
		double difference;
		if (number > target) {
			difference = number - target;
		} else {
			difference = target - number;
		}
		return difference <= range;
	}
	
	public static boolean voxelShapeEligibleForGrab(VoxelShape shape, Direction facing) {
		double xMin = shape.getMin(Direction.Axis.X);
		double xMax = shape.getMax(Direction.Axis.X);
		
		double yMin = shape.getMin(Direction.Axis.Y);
		double yMax = shape.getMax(Direction.Axis.Y);
		
		double zMin = shape.getMin(Direction.Axis.Z);
		double zMax = shape.getMax(Direction.Axis.Z);
		
		// this is unholy
		// iterate over each pixel of a 16x16x16 block, checking for a solid row across which can be grabbed.
		int solidPixelsInARow = 0;
		return switch (facing) {
			case NORTH -> {
				// x+, z-
				for (double y = yMax; y > yMin; y -= SIXTEENTH) {
					for (double x = xMin; x < xMax; x += SIXTEENTH) {
						for (double z = zMax; z >= zMin; z -= SIXTEENTH) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			case EAST -> {
				// x+, z+
				for (double y = yMax; y > yMin; y -= SIXTEENTH) {
					for (double x = xMin; x < xMax; x += SIXTEENTH) {
						for (double z = zMin; z < zMax; z += SIXTEENTH) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			case SOUTH -> {
				// x-, z+
				for (double y = yMax; y > yMin; y -= SIXTEENTH) {
					for (double x = xMax; x >= xMin; x -= SIXTEENTH) {
						for (double z = zMin; z < zMax; z += SIXTEENTH) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			case WEST -> {
				// x-, z-
				for (double y = yMax; y > yMin; y -= SIXTEENTH) {
					for (double x = xMax; x >= xMin; x -= SIXTEENTH) {
						for (double z = zMax; z >= zMin; z -= SIXTEENTH) {
							if (shape.getBoundingBox().contains(xMin + x, yMin + y, zMin + z)) {
								solidPixelsInARow++;
							} else {
								solidPixelsInARow = 0;
							}
							if (solidPixelsInARow == 16) {
								yield true;
							}
						}
					}
				}
				yield false;
			}
			default -> throw new RuntimeException("this should never be called, if it did something has gone catastrophically wrong");
		};
	}
	
	public interface PlayerEntityExtensions {
		void wahoo$setBonked(boolean value, UUID bonked);
		boolean wahoo$getSliding();
	}
	
	public interface ServerPlayerEntityExtensions {
		void wahoo$setPreviousJumpType(JumpTypes type);
		void wahoo$setGroundPounding(boolean value, boolean breakBlocks);
		void wahoo$setDiving(boolean value, @Nullable BlockPos startPos);
		void wahoo$setSliding(boolean value);
		void wahoo$setDestructionPermOverride(boolean value);
		void wahoo$setCaptured(@Nullable NbtCompound capturedData);
		NbtCompound wahoo$getCaptured();
	}
	
	public interface ClientPlayerEntityExtensions {
		boolean wahoo$groundPounding();
		boolean wahoo$slidingOnSlope();
		boolean wahoo$slidingOnGround();
	}
	
	public interface KeyboardInputExtensions {
		void wahoo$disableControl();
		void wahoo$enableControl();
	}
}
