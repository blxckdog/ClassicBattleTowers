package blxckdog.battletowers;

import java.util.List;
import java.util.Set;

import blxckdog.battletowers.entity.TowerGolemEntity;
import blxckdog.battletowers.entity.TowerGolemFireballEntity;
import blxckdog.battletowers.world.BattleTowerDestructionManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class ClassicBattleTowers implements ModInitializer {

    /*
     * Entities
     */
    public static final RegistryKey<EntityType<?>> BATTLE_TOWER_GOLEM_KEY = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE,
            id("battle_tower_golem")
    );

    public static final EntityType<TowerGolemEntity> BATTLE_TOWER_GOLEM = EntityType.Builder.create(
                    TowerGolemEntity::new, SpawnGroup.MONSTER
            )
            .dimensions(1.7f, 4f)
            .build(BATTLE_TOWER_GOLEM_KEY);

    public static final RegistryKey<EntityType<?>> BATTLE_TOWER_GOLEM_FIREBALL_KEY = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE,
            id("battle_tower_golem_fireball")
    );

    public static final EntityType<TowerGolemFireballEntity> BATTLE_TOWER_GOLEM_FIREBALL = EntityType.Builder.create(
                    TowerGolemFireballEntity::new, SpawnGroup.MISC
            )
            .dimensions(.4f, .4f)
            .build(BATTLE_TOWER_GOLEM_FIREBALL_KEY);

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
        return Identifier.of("battletowers", path);
    }


    @Override
    public void onInitialize() {
        registerEntities();
        registerAttributes();
        registerSoundEvents();
        registerGolemSpawner();
        registerGolemWakeupListener();
    }

    private void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE, BATTLE_TOWER_GOLEM_KEY, BATTLE_TOWER_GOLEM);
        Registry.register(Registries.ENTITY_TYPE, BATTLE_TOWER_GOLEM_FIREBALL_KEY, BATTLE_TOWER_GOLEM_FIREBALL);
    }

    private void registerAttributes() {
        FabricDefaultAttributeRegistry.register(BATTLE_TOWER_GOLEM, TowerGolemEntity.createTowerGolemAttributes());
        BattleTowerDestructionManager.registerTickEvent();
    }

    private void registerSoundEvents() {
        registerSound("golem_ambient", SOUND_GOLEM_AMBIENT);
        registerSound("golem_awaken", SOUND_GOLEM_AWAKEN);
        registerSound("golem_death", SOUND_GOLEM_DEATH);
        registerSound("golem_hurt", SOUND_GOLEM_HURT);
        registerSound("golem_special", SOUND_GOLEM_SPECIAL);
        registerSound("golem_charge", SOUND_GOLEM_CHARGE);

        registerSound("tower_crumble", SOUND_TOWER_CRUMBLE);
        registerSound("tower_break_start", SOUND_TOWER_BREAK_START);
    }

    private void registerSound(String name, SoundEvent soundEvent) {
        Registry.register(Registries.SOUND_EVENT, id(name), soundEvent);
    }

    private void registerGolemSpawner() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof MarkerEntity)) return;

            Set<String> entityTags = entity.getCommandTags();
            if (!entityTags.contains("battletowers.summon.default_golem") &&
                    !entityTags.contains("battletowers.summon.default_golem_underground")) {
                return;
            }

            TowerGolemEntity golem = new TowerGolemEntity(BATTLE_TOWER_GOLEM, world.toServerWorld());
            golem.setPosition(entity.getPos());
            golem.setTowerPosition(entity.getBlockPos());

            if (entityTags.contains("battletowers.summon.default_golem_underground")) {
                golem.setTowerUnderground(true);
            }

            world.spawnEntity(golem);
            entity.discard();
        });
    }

    private void registerGolemWakeupListener() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Wake up Battle Tower Golem only on server side
            if (world.isClient) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (!state.isOf(Blocks.CHEST) && !state.isOf(Blocks.HOPPER)) {
                return ActionResult.PASS;
            }

            List<TowerGolemEntity> nearbyGolems = world.getEntitiesByClass(
                    TowerGolemEntity.class,
                    new Box(pos).expand(10),
                    golem -> true
            );

            if (!nearbyGolems.isEmpty()) {
                nearbyGolems.forEach(golem -> {
                    golem.wakeUpGolem();
                    golem.setTarget(player);
                });
                // Prevent the chest from being opened
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });


        // Listen for chest or hopper break to wake up Battle Tower Golem
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
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

                // Prevent the chest from being broken
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

    }

}