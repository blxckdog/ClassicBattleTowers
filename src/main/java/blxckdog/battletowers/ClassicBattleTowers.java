package blxckdog.battletowers;

import java.util.List;
import java.util.Set;

import blxckdog.battletowers.entity.TowerGolemEntity;
import blxckdog.battletowers.entity.TowerGolemFireballEntity;
import blxckdog.battletowers.world.BattleTowerDestructionManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class ClassicBattleTowers implements ModInitializer {
	
	/*
	 * Entities
	 */
	public static final EntityType<TowerGolemEntity> BATTLE_TOWER_GOLEM = Registry.register(
			Registries.ENTITY_TYPE, 
			id("battle_tower_golem"),
			FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TowerGolemEntity::new)
					.dimensions(EntityDimensions.fixed(1.7f, 4f))
					.build()
			);
	
	public static final EntityType<TowerGolemFireballEntity> BATTLE_TOWER_GOLEM_FIREBALL = Registry.register(
			Registries.ENTITY_TYPE, 
			id("battle_tower_golem_fireball"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, TowerGolemFireballEntity::new)
					.dimensions(EntityDimensions.fixed(.4f, .4f))
					.build()
			);

	/*
	 * Sounds
	 */
	public static final SoundEvent SOUND_GOLEM_AMBIENT = SoundEvent.of(id("golem_ambient"));
	public static final SoundEvent SOUND_GOLEM_AWAKEN = SoundEvent.of(id("golem_awaken"));
	public static final SoundEvent SOUND_GOLEM_DEATH = SoundEvent.of(id("golem_death"));
	public static final SoundEvent SOUND_GOLEM_HURT = SoundEvent.of(id("golem_hurt"));
	public static final SoundEvent SOUND_GOLEM_SPECIAL = SoundEvent.of(id("golem_special"));
	public static final SoundEvent SOUND_GOLEM_CHARGE = SoundEvent.of(id("golem_charge"));
	
	public static final SoundEvent SOUND_TOWER_CRUMBLE = SoundEvent.of(id("tower_crumble"));
	public static final SoundEvent SOUND_TOWER_BREAK_START = SoundEvent.of(id("tower_break_start"));
	
	
	public static Identifier id(String path) {
		return new Identifier("battletowers", path);
	}
	
	
	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.register(BATTLE_TOWER_GOLEM, TowerGolemEntity.createTowerGolemAttributes());
		BattleTowerDestructionManager.registerTickEvent();
		
		// Register sound events
		Registry.register(Registries.SOUND_EVENT, id("golem_ambient"), SOUND_GOLEM_AMBIENT);
		Registry.register(Registries.SOUND_EVENT, id("golem_awaken"), SOUND_GOLEM_AWAKEN);
		Registry.register(Registries.SOUND_EVENT, id("golem_death"), SOUND_GOLEM_DEATH);
		Registry.register(Registries.SOUND_EVENT, id("golem_hurt"), SOUND_GOLEM_HURT);
		Registry.register(Registries.SOUND_EVENT, id("golem_special"), SOUND_GOLEM_SPECIAL);
		Registry.register(Registries.SOUND_EVENT, id("golem_charge"), SOUND_GOLEM_CHARGE);
		
		Registry.register(Registries.SOUND_EVENT, id("tower_crumble"), SOUND_TOWER_CRUMBLE);
		Registry.register(Registries.SOUND_EVENT, id("tower_break_start"), SOUND_TOWER_BREAK_START);
		
		// Spawn Battle Tower Golem
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {			
			if(!(entity instanceof MarkerEntity)) {
				return;
			}

			Set<String> entityTags = entity.getCommandTags();

			if(!entityTags.contains("battletowers.summon.default_golem") &&
					!entityTags.contains("battletowers.summon.default_golem_underground")) {
				return;
			}

			TowerGolemEntity battleTowerGolem = new TowerGolemEntity(BATTLE_TOWER_GOLEM, world.toServerWorld());
			battleTowerGolem.setPosition(entity.getPos());
			battleTowerGolem.setTowerPosition(entity.getBlockPos());

			if(entityTags.contains("battletowers.summon.default_golem_underground")) {
				battleTowerGolem.setTowerUnderground(true);
			}

			// Replace marker with Battle Tower Golem
			world.spawnEntity(battleTowerGolem);
			entity.remove(RemovalReason.DISCARDED);
		});
		
		// Listen for chest or hopper use to wake up Battle Tower Golem
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);
			
			if(world.isClient) {
				return ActionResult.PASS;
			}
			
			if(!state.isOf(Blocks.CHEST) && !state.isOf(Blocks.HOPPER)) {
				return ActionResult.PASS;
			}
			
			List<TowerGolemEntity> nearbyGolems = world.getEntitiesByClass(
					TowerGolemEntity.class, 
					new Box(pos).expand(10),
					golem -> true
				);
		
			if(!nearbyGolems.isEmpty()) {
				// Wake up Battle Tower Golem only on server side
				if(!world.isClient) {
					nearbyGolems.forEach(golem -> {
						golem.wakeUpGolem();
						golem.setTarget(player);
					});
				}
				
				// Prevent the chest from being opened
				return ActionResult.FAIL;
			}
			
			return ActionResult.PASS;
		});
	}
}