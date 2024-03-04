package blxckdog.battletowers.entity.ai;

import blxckdog.battletowers.ClassicBattleTowers;
import blxckdog.battletowers.entity.TowerGolemEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.sound.SoundCategory;

public class TowerGolemShootGoal extends Goal {

	private final TowerGolemEntity golem;
	private final float maxShootRange;
	private final int intervalTicks;
	
	private LivingEntity target;
	private int updateCountdownTicks = 0;
	
	
	public TowerGolemShootGoal(TowerGolemEntity golem, int intervalTicks, float maxShootRange) {
		this.golem = golem;
		this.maxShootRange = maxShootRange;
		this.intervalTicks = intervalTicks;
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
		updateCountdownTicks = 0;
	}
	
	@Override
	public boolean shouldContinue() {
		return canStart() || target.isAlive();
	}

	public boolean shouldRunEveryTick() {
		return true;
	}

	public void tick() {
		double delta = golem.squaredDistanceTo(target);
		boolean visible = golem.getVisibilityCache().canSee(target);
		
		if(--updateCountdownTicks == 10) {
			if(delta - maxShootRange*maxShootRange <= 0) {
				golem.getWorld().playSound(golem, golem.getBlockPos(), ClassicBattleTowers.SOUND_GOLEM_CHARGE, SoundCategory.HOSTILE, 1f, 1f);
			}
		} else if (updateCountdownTicks == 0) {
			if(delta - maxShootRange*maxShootRange <= 0) {
				golem.shootAt(target, visible ? 1f : 1.9f);
			}
			
			updateCountdownTicks = intervalTicks;
		} else if (updateCountdownTicks < 0) {
			updateCountdownTicks = intervalTicks;
		}

	}
	
}
