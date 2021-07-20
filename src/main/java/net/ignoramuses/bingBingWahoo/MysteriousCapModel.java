// Made with Model Converter by Globox_Z
// Generate all required imports
// Made with Blockbench 3.9.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package net.ignoramuses.bingBingWahoo;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityModelLayerRegistry;
import net.ignoramuses.bingBingWahoo.mixin.ModelPartAccessor;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class MysteriousCapModel extends Model {
	public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier(BingBingWahoo.ID, "mysterious_cap"), "cap");
	private final ModelPart bone; // everything
	private final ModelPart cube_r1; // that one slanted cube
	public final Model wearerModel;
	
	public MysteriousCapModel(EntityRendererFactory.Context ctx, Model playerModel) {
		super(RenderLayer::getEntityCutoutNoCull);
		bone = ctx.getPart(MODEL_LAYER).getChild("bone");
		cube_r1 = bone.getChild("cube_r1");
		this.wearerModel = playerModel;
		setRotationAngle(cube_r1, 0.0F, 0.0F, -0.3927F);
	}
	
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData modelPartData1 = modelPartData.addChild("bone", ModelPartBuilder.create().uv(0,0).cuboid(-12.0F, -3.0F, 4.0F, 9.0F, 3.0F, 9.0F).uv(0,0).cuboid(-12.0F, -5.0F, 4.0F, 9.0F, 3.0F, 9.0F, new Dilation(-0.01F)).uv(0,23).cuboid(-7.0F, -4.05F, 4.0F, 4.0F, 1.0F, 9.0F).uv(0,0).cuboid(-3.75F, -3.75F, 7.5F, 1.0F, 2.0F, 2.0F).uv(23,14).cuboid(-3.075F, -1.0F, 4.0F, 4.0F, 1.0F, 9.0F), ModelTransform.pivot(5.5F,24.0F,-8.5F));
		modelPartData1.addChild("cube_r1", ModelPartBuilder.create().uv(0,12).cuboid(-3.7F, -1.6F, -4.5F, 7.0F, 2.0F, 9.0F, new Dilation(0.01F)), ModelTransform.pivot(-6.1945F,-3.9853F,8.5F));
		return TexturedModelData.of(modelData,64,64);
	}
	
	@Override
	public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, 1);
	}
	
	public void setRotationAngle(ModelPart bone, float x, float y, float z) {
		bone.pitch = x;
		bone.yaw = y;
		bone.roll = z;
	}
}