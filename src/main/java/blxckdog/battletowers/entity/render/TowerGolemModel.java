package blxckdog.battletowers.entity.render;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;

public class TowerGolemModel extends BipedEntityModel<TowerGolemRenderState> {

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = getModelData(Dilation.NONE, 0);
		return TexturedModelData.of(modelData, 64, 32);
	}
	
	public TowerGolemModel(ModelPart root) {
		super(root);
	}

}
