package blxckdog.battletowers.entity.render;

import static blxckdog.battletowers.ClassicBattleTowers.id;

import blxckdog.battletowers.ClassicBattleTowersClient;
import blxckdog.battletowers.entity.TowerGolemEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TowerGolemRenderer extends BipedEntityRenderer<TowerGolemEntity, TowerGolemRenderState, TowerGolemModel>{

	private static final Identifier TEXTURE_DORMANT = id("textures/model/tower_golem_dormant.png");
	private static final Identifier TEXTURE_AWAKE = id("textures/model/tower_golem.png");
	
	
	public TowerGolemRenderer(Context context) {
		super(context, new TowerGolemModel(context.getPart(ClassicBattleTowersClient.MODEL_TOWER_GOLEM_LAYER)), 0.95f);
	}

	@Override
	public TowerGolemRenderState createRenderState() {
		return new TowerGolemRenderState();
	}

	@Override
	public void updateRenderState(TowerGolemEntity golem, TowerGolemRenderState state, float tickDelta) {
		super.updateRenderState(golem, state, tickDelta);
		state.isDormant = golem.isDormant();
	}

	@Override
	public Identifier getTexture(TowerGolemRenderState state) {
		return state.isDormant ? TEXTURE_DORMANT : TEXTURE_AWAKE;
	}

	@Override
	public void render(TowerGolemRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.scale(2f, 2f, 2f);
		matrices.translate(0f, 0f, 0f);

		super.render(state, matrices, vertexConsumers, light);
	}

}
