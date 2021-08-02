package net.ignoramuses.bingBingWahoo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class WahooUtils {
	public static final double SIXTEENTH = 1D / 16;
	public static double getVelocityForSlopeDirection(Direction directionOfSlope) {
		return switch (directionOfSlope) {
			case NORTH, WEST -> 0.1;
			case SOUTH, EAST -> -0.1;
			default -> throw new IllegalStateException("Unexpected value: " + directionOfSlope);
		};
	}
	
	public static double getVelocityForSlidingOnGround(Direction looking) {
		return switch (looking) {
			case NORTH, WEST -> -0.07;
			case SOUTH, EAST -> 0.07;
			default -> throw new IllegalStateException("Unexpected value: " + looking);
		};
	}
	
	public static double getMaxWithSign(double value, double max) {
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
	public static boolean approximately(double number, double target, double range) {
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
}
