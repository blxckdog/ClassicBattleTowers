package blxckdog.battletowers.entity;

import blxckdog.battletowers.ClassicBattleTowers;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

public class TowerGolemFireballEntity extends AbstractFireballEntity {

	private final float explosionPower;
	
	
	public TowerGolemFireballEntity(EntityType<TowerGolemFireballEntity> type, World world) {
		super(type, world);
		explosionPower = 1f;
	}
	
	protected TowerGolemFireballEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ, float explosionPower) {
		super(ClassicBattleTowers.BATTLE_TOWER_GOLEM_FIREBALL, owner, velocityX, velocityY, velocityZ, world);
		this.explosionPower = explosionPower;
	}
	
	
	@Override
	public void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);

		if (getWorld().isClient) {
			return;
		}
		
		Entity hitEntity = entityHitResult.getEntity();
		Entity thrower = this.getOwner();
		
		BlockPos targetPos = hitEntity.getBlockPos();
		createExplosion(targetPos);
	}
	
	@Override
	public void onBlockHit(BlockHitResult blockHitResult) {
		super.onBlockHit(blockHitResult);
		
		if (getWorld().isClient) {
			return;
		}
		
		BlockPos targetPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
		createExplosion(targetPos);
		
		if (getWorld().isAir(targetPos)) {
			getWorld().setBlockState(targetPos, AbstractFireBlock.getState(getWorld(), targetPos));
		}
	}
	
	private void createExplosion(BlockPos pos) {
		Entity thrower = this.getOwner();
		
		if (!(thrower instanceof MobEntity) || getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
			getWorld().createExplosion(thrower, pos.getX(), pos.getY(), pos.getZ(), explosionPower, ExplosionSourceType.MOB);
		}
	}
	
	
	public void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		
		if (!getWorld().isClient) {
			this.discard();
		}

	}

	public boolean canHit() {
		return false;
	}

	public boolean damage(DamageSource source, float amount) {
		return false;
	}
	
}
