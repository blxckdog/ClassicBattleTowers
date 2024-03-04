package blxckdog.battletowers.entity.render;

import blxckdog.battletowers.entity.TowerGolemEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class TowerGolemModel extends BipedEntityModel<TowerGolemEntity> {

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = getModelData(Dilation.NONE, 0);
		return TexturedModelData.of(modelData, 64, 32);
	}
	
	public TowerGolemModel(ModelPart root) {
		super(root);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		matrices.scale(2f, 2f, 2f);
		matrices.translate(0f, -0.75f, 0f);
		
		// Call delegated render method
		super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}

}
