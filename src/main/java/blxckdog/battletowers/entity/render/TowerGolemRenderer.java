package blxckdog.battletowers.entity.render;

import static blxckdog.battletowers.ClassicBattleTowers.id;

import blxckdog.battletowers.ClassicBattleTowersClient;
import blxckdog.battletowers.entity.TowerGolemEntity;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;

public class TowerGolemRenderer extends BipedEntityRenderer<TowerGolemEntity, TowerGolemModel>{

	private static final Identifier TEXTURE_DORMANT = id("textures/model/tower_golem_dormant.png");
	private static final Identifier TEXTURE_AWAKE = id("textures/model/tower_golem.png");
	
	
	public TowerGolemRenderer(Context context) {
		super(context, new TowerGolemModel(context.getPart(ClassicBattleTowersClient.MODEL_TOWER_GOLEM_LAYER)), 0.95f);
	}

	@Override
	public Identifier getTexture(TowerGolemEntity golem) {
		return golem.isDormant() ? TEXTURE_DORMANT : TEXTURE_AWAKE;
	}

}
