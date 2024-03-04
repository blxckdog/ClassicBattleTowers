package blxckdog.battletowers;

import static blxckdog.battletowers.ClassicBattleTowers.id;

import blxckdog.battletowers.entity.TowerGolemFireballEntity;
import blxckdog.battletowers.entity.render.TowerGolemModel;
import blxckdog.battletowers.entity.render.TowerGolemRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;

@Environment(EnvType.CLIENT)
public class ClassicBattleTowersClient implements ClientModInitializer {

	public static final EntityModelLayer MODEL_TOWER_GOLEM_LAYER = new EntityModelLayer(id("tower_golem"), "main");
	
	@Override
	public void onInitializeClient() {
		// Register rendering for Battle Tower Golem
		EntityRendererRegistry.register(ClassicBattleTowers.BATTLE_TOWER_GOLEM, (context) -> {
			return new TowerGolemRenderer(context);
		});
		
		EntityModelLayerRegistry.registerModelLayer(MODEL_TOWER_GOLEM_LAYER, TowerGolemModel::getTexturedModelData);
		
		// Register rendering for Battle Tower Golem Fireball Projectile
		EntityRendererRegistry.register(ClassicBattleTowers.BATTLE_TOWER_GOLEM_FIREBALL, (context) -> {
			return new FlyingItemEntityRenderer<TowerGolemFireballEntity>(context);
		});
	}
	
}
