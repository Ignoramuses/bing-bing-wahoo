package io.github.ignoramuses.bing_bing_wahoo;

import com.mojang.math.Axis;
import io.github.ignoramuses.bing_bing_wahoo.content.movement.Slopes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import io.github.ignoramuses.bing_bing_wahoo.content.cap.MysteriousCapModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import static io.github.ignoramuses.bing_bing_wahoo.WahooRegistry.MYSTERIOUS_CAP;

public class WahooUtils {
	public static final double SIXTEENTH = 1 / 16f;
	public static final ResourceLocation CAP_TEXTURE = new ResourceLocation(BingBingWahoo.ID, "textures/armor/mysterious_cap.png");
	public static final ResourceLocation EMBLEM_TEXTURE = new ResourceLocation(BingBingWahoo.ID, "textures/armor/mysterious_cap_emblem.png");
	
	public static ListTag toNbtList(double... values) {
		ListTag nbtList = new ListTag();
		
		for(double d : values) {
			nbtList.add(DoubleTag.valueOf(d));
		}
		
		return nbtList;
	}
	
	@Environment(EnvType.CLIENT)
	public static void renderCap(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack hatStack, int light, float tickDelta, MysteriousCapModel model) {
		int color = MYSTERIOUS_CAP.getColor(hatStack);
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		matrices.pushPose();
		matrices.mulPose(Axis.YP.rotationDegrees(90));
		VertexConsumer capConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(CAP_TEXTURE), false, hatStack.hasFoil());
		model.renderToBuffer(matrices, capConsumer, light, 1, r, g, b, 1);
		VertexConsumer emblemConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(EMBLEM_TEXTURE), false, hatStack.hasFoil());
		model.renderToBuffer(matrices, emblemConsumer, light, 1, 1, 1, 1, 1);
		matrices.popPose();
	}
	
	@Nullable
	@Environment(EnvType.CLIENT)
	public static ModelPart getHeadModel(@Nullable Model base) {
		if (base instanceof HumanoidModel biped) {
			return biped.head;
		} else if (base instanceof HeadedModel model) {
			return model.getHead();
		}
		
		return null;
	}
	
	public static double getVelocityForSlopeDirection(Direction directionOfSlope) {
		return switch (directionOfSlope) {
			case NORTH, WEST -> -0.1;
			case SOUTH, EAST -> 0.1;
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
	
	public static Direction getHorizontalDirectionFromVector(Vec3 vector) {
		double x = vector.x();
		double z = vector.z();
		
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
		double xMin = shape.min(Direction.Axis.X);
		double xMax = shape.max(Direction.Axis.X);
		
		double yMin = shape.min(Direction.Axis.Y);
		double yMax = shape.max(Direction.Axis.Y);
		
		double zMin = shape.min(Direction.Axis.Z);
		double zMax = shape.max(Direction.Axis.Z);
		
		// this is unholy
		// iterate over each pixel of a 16x16x16 block, checking for a solid row across which can be grabbed.
		int solidPixelsInARow = 0;
		return switch (facing) {
			case NORTH -> {
				// x+, z-
				for (double y = yMax; y > yMin; y -= SIXTEENTH) {
					for (double x = xMin; x < xMax; x += SIXTEENTH) {
						for (double z = zMax; z >= zMin; z -= SIXTEENTH) {
							if (shape.bounds().contains(xMin + x, yMin + y, zMin + z)) {
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
							if (shape.bounds().contains(xMin + x, yMin + y, zMin + z)) {
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
							if (shape.bounds().contains(xMin + x, yMin + y, zMin + z)) {
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
							if (shape.bounds().contains(xMin + x, yMin + y, zMin + z)) {
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
