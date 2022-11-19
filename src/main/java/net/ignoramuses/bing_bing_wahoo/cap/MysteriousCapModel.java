// Made with Model Converter by Globox_Z
// Generate all required imports
// Made with Blockbench 3.9.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package net.ignoramuses.bing_bing_wahoo.cap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ignoramuses.bing_bing_wahoo.BingBingWahoo;
import net.ignoramuses.bing_bing_wahoo.WahooUtils;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MysteriousCapModel extends Model {
	public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(new ResourceLocation(BingBingWahoo.ID, "mysterious_cap"), "cap");
	private final ModelPart bone; // everything
	private final ModelPart cube_r1; // that one slanted cube
	@Nullable
	public final EntityModel<?> wearerModel;
	@Nullable
	public final ModelPart wearerHead;
	
	public MysteriousCapModel(EntityRendererProvider.Context ctx, @Nullable EntityModel<?> wearerModel) {
		super(RenderType::entityCutoutNoCull);
		bone = ctx.bakeLayer(MODEL_LAYER).getChild("bone");
		cube_r1 = bone.getChild("cube_r1");
		this.wearerModel = wearerModel;
		this.wearerHead = WahooUtils.getHeadModel(wearerModel);
		setRotationAngle(cube_r1, 0.0F, 0.0F, -0.3927F);
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition modelData = new MeshDefinition();
		PartDefinition modelPartData = modelData.getRoot();
		PartDefinition modelPartData1 = modelPartData.addOrReplaceChild("bone",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-12.0F, -3.0F, 4.0F, 9.0F, 3.0F, 9.0F)
						.texOffs(0, 0)
						.addBox(-12.0F, -5.0F, 4.0F, 9.0F, 3.0F, 9.0F, new CubeDeformation(-0.01F))
						.texOffs(0, 23)
						.addBox(-7.0F, -4.05F, 4.0F, 4.0F, 1.0F, 9.0F)
						.texOffs(0, 0)
						.addBox(-3.75F, -3.75F, 7.5F, 1.0F, 2.0F, 2.0F)
						.texOffs(23, 14)
						.addBox(-3.075F, -1.0F, 4.0F, 4.0F, 1.0F, 9.0F),
				PartPose.offset(5.5F, 24.0F, -8.5F)
		);
		modelPartData1.addOrReplaceChild("cube_r1",
				CubeListBuilder.create()
						.texOffs(0, 12)
						.addBox(-3.7F, -1.6F, -4.5F, 7.0F, 2.0F, 9.0F, new CubeDeformation(0.01F)),
				PartPose.offset(-6.1945F, -3.9853F, 8.5F)
		);
		return LayerDefinition.create(modelData, 64, 64);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, 1);
	}
	
	public void setRotationAngle(ModelPart bone, float x, float y, float z) {
		bone.xRot = x;
		bone.yRot = y;
		bone.zRot = z;
	}
}