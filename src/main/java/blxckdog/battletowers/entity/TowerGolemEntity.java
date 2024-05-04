package blxckdog.battletowers.entity;

import blxckdog.battletowers.ClassicBattleTowers;
import blxckdog.battletowers.entity.ai.NotDormantGoalProxy;
import blxckdog.battletowers.entity.ai.TowerGolemShootGoal;
import blxckdog.battletowers.entity.ai.TowerGolemSleepGoal;
import blxckdog.battletowers.entity.ai.TowerGolemStompGoal;
import blxckdog.battletowers.world.BattleTowerDestructionManager;
import blxckdog.battletowers.world.BattleTowerDestructionTask;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class TowerGolemEntity extends HostileEntity implements RangedAttackMob {

    protected static final TrackedData<BlockPos> TOWER_POS;
    protected static final TrackedData<Boolean> DORMANT;
    protected static final TrackedData<Boolean> UNDERGROUND;

    static {
        TOWER_POS = DataTracker.registerData(TowerGolemEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
        DORMANT = DataTracker.registerData(TowerGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        UNDERGROUND = DataTracker.registerData(TowerGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public static DefaultAttributeContainer.Builder createTowerGolemAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.7)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16)
                .add(EntityAttributes.GENERIC_ARMOR, 1)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 2)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1);
    }


    public TowerGolemEntity(EntityType<TowerGolemEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TOWER_POS, BlockPos.ORIGIN);
        builder.add(UNDERGROUND, false);
        builder.add(DORMANT, true);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));

        // Attacking goals
        goalSelector.add(1, whenAwake(new MeleeAttackGoal(this, 1d, true)));
        goalSelector.add(2, whenAwake(new TowerGolemShootGoal(this, 20 * 3, 20f)));
        goalSelector.add(3, whenAwake(new TowerGolemStompGoal(this, 20 * 5, 6f)));

        // Ambient goals
        goalSelector.add(4, whenAwake(new LookAtEntityGoal(this, PlayerEntity.class, 12f)));
        goalSelector.add(5, whenAwake(new TowerGolemSleepGoal(this, 20 * 15)));

        // Targeting
        targetSelector.add(1, whenAwake(new RevengeGoal(this)));
        targetSelector.add(2, whenAwake(new ActiveTargetGoal<>(this, PlayerEntity.class, false)));
    }

    private Goal whenAwake(Goal goal) {
        return new NotDormantGoalProxy(this, goal);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (!getWorld().isClient && getTowerPosition() != BlockPos.ORIGIN) {
            // Destroy Tower
            BattleTowerDestructionManager.registerTask(new BattleTowerDestructionTask(getWorld(), getTowerPosition(), isTowerUnderground()));

            Text deathText = Text.translatable("notify.battletowers.golem_defeated");
            Objects.requireNonNull(getServer()).getPlayerManager().broadcast(deathText, false);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getSource() != null) {
            Entity attacker = source.getSource();

            if (source.isIndirect()) {
                attacker = source.getAttacker();
            }

            if (attacker instanceof LivingEntity) {
                // Wake up Battle Tower Golem when player attacks from further away
                setTarget((LivingEntity) attacker);
                wakeUpGolem();
            }
        }

        return super.damage(source, amount);
    }

    @Override
    public void shootAt(LivingEntity target, float power) {
        double srcX = getX();
        double srcY = getEyeY();
        double srcZ = getZ();

        double dstX = target.getX() - srcX;
        double dstY = target.getY() + (target.getStandingEyeHeight() * .5) - srcY;
        double dstZ = target.getZ() - srcZ;

        TowerGolemFireballEntity fireball = new TowerGolemFireballEntity(getWorld(), this, dstX, dstY, dstZ, power);
        fireball.setPos(srcX, srcY, srcZ);
        fireball.setOwner(this);

        getWorld().playSound(this, getBlockPos(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, getSoundVolume(), 1f);
        getWorld().spawnEntity(fireball);
    }

    @Override
    public void tick() {
        super.tick();

        // Check for victims within 6 blocks
        if (isDormant()) {
            PlayerEntity player = getWorld().getClosestPlayer(getX(), getY(), getZ(), 6d, true);

            if (player != null && this.canSee(player)) {
                setTarget(player);
                wakeUpGolem();
            }
        }
    }


    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public int getSafeFallDistance() {
        return 999;
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ClassicBattleTowers.SOUND_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ClassicBattleTowers.SOUND_GOLEM_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ClassicBattleTowers.SOUND_GOLEM_AMBIENT;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 20 * 20;
    }


    public void wakeUpGolem() {
        if (isDormant()) {
            getWorld().playSound(this, getBlockPos(), ClassicBattleTowers.SOUND_GOLEM_AWAKEN, SoundCategory.HOSTILE, 2f, 1f);
        }

        this.dataTracker.set(DORMANT, false);
    }

    public void setDormant(boolean value) {
        this.dataTracker.set(DORMANT, value);
    }

    public boolean isDormant() {
        return this.dataTracker.get(DORMANT);
    }

    public BlockPos getTowerPosition() {
        return this.dataTracker.get(TOWER_POS);
    }

    public void setTowerPosition(BlockPos towerPos) {
        this.dataTracker.set(TOWER_POS, towerPos);
    }

    public void setTowerUnderground(boolean value) {
        this.dataTracker.set(UNDERGROUND, value);
    }

    public boolean isTowerUnderground() {
        return this.dataTracker.get(UNDERGROUND);
    }


    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("TowerX") && nbt.contains("TowerY") && nbt.contains("TowerZ")) {
            int spawnX = nbt.getInt("TowerX");
            int spawnY = nbt.getInt("TowerY");
            int spawnZ = nbt.getInt("TowerZ");

            setTowerPosition(new BlockPos(spawnX, spawnY, spawnZ));
        }

        if(nbt.contains("TowerUnderground")) {
            setTowerUnderground(nbt.getBoolean("TowerUnderground"));
        }

        if (nbt.contains("Dormant")) {
            setDormant(nbt.getBoolean("Dormant"));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Dormant", isDormant());
        nbt.putBoolean("TowerUnderground", isTowerUnderground());

        BlockPos spawnPos = getTowerPosition();
        nbt.putInt("TowerX", spawnPos.getX());
        nbt.putInt("TowerY", spawnPos.getY());
        nbt.putInt("TowerZ", spawnPos.getZ());
    }

}
