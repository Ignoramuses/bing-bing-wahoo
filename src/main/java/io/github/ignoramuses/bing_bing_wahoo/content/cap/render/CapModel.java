package io.github.ignoramuses.bing_bing_wahoo.content.cap.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

// remake in progress
public class CapModel extends Model {
	private final ModelPart bone; // everything
	private final ModelPart cube_r1; // that one slanted cube

	public CapModel() {
		super(RenderType::entityCutoutNoCull);
		this.bone = getTexturedModelData().bakeRoot().getChild("bone");
		this.cube_r1 = bone.getChild("cube_r1");
		setRotationAngle(cube_r1, 0.0F, 0.0F, -0.3927F);
	}

	protected LayerDefinition getTexturedModelData() {
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
