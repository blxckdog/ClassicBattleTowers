package blxckdog.battletowers.entity.ai;

import blxckdog.battletowers.ClassicBattleTowers;
import blxckdog.battletowers.entity.TowerGolemEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World.ExplosionSourceType;

public class TowerGolemStompGoal extends Goal {

	private final TowerGolemEntity golem;
	private final int intervallTicks;
	private final float minDistance;
	
	private LivingEntity target;
	private boolean doStompAttack = false;
	private int explosionCounter = 0;
	private int rageCounter = 0;
	
	
	public TowerGolemStompGoal(TowerGolemEntity golem, int intervallTicks, float minDistance) {
		this.golem = golem;
		this.intervallTicks = intervallTicks;
		this.minDistance = minDistance;
	}
	
	
	@Override
	public boolean canStart() {
		LivingEntity livingEntity = golem.getTarget();
		
		if (livingEntity != null && livingEntity.isAlive()) {
			target = livingEntity;
			return true;
		} else {
			return false;
		}
	}
	
	public void stop() {
		target = null;
		rageCounter = 0;
		explosionCounter = 0;
		doStompAttack = false;
	}
	
	@Override
	public boolean shouldContinue() {
		return canStart() || target.isAlive();
	}

	public boolean shouldRunEveryTick() {
		return true;
	}
	
	
	@Override
	public void tick() {
		boolean targetNearby = golem.squaredDistanceTo(target) < minDistance*minDistance;
		
		// Increase rage counter if last successful attack is some time ago???
		rageCounter = (!targetNearby || doStompAttack) ? rageCounter+1 : 0;
		
		// Do stomp attack after interval ticks at distance
		if(rageCounter > intervallTicks && !doStompAttack) {
			golem.setVelocity(golem.getVelocity().add(new Vec3d(0, 0.9d, 0)));
			golem.getWorld().playSound(golem, golem.getBlockPos(), ClassicBattleTowers.SOUND_GOLEM_SPECIAL, SoundCategory.HOSTILE, 4f, 1f);
			doStompAttack = true;
			return;
		}
		
		// Wait max 1 second (20 ticks) or if golem is on ground to create explosion
		if((rageCounter > intervallTicks+20 || golem.isOnGround()) && doStompAttack) {
			if(golem.getHealth() < golem.getMaxHealth() / 2) {
				golem.setHealth(golem.getHealth() + 20);
			}

			// Make explosions less likely
			if(explosionCounter <= 0) {
				golem.getWorld().createExplosion(golem, golem.getX(), golem.getY(), golem.getZ(), 4f, ExplosionSourceType.MOB);
				explosionCounter = 3;
			}
			doStompAttack = false;
			explosionCounter--;
			rageCounter = 0;
		}
	}

}
