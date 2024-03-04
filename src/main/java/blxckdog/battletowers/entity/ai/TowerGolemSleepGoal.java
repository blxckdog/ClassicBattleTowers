package blxckdog.battletowers.entity.ai;

import blxckdog.battletowers.entity.TowerGolemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class TowerGolemSleepGoal extends Goal {

	private final TowerGolemEntity golem;
	private final int intervalTicks;
	
	private LivingEntity target;
	private int noTargetCounter = 20*12;
	
	
	public TowerGolemSleepGoal(TowerGolemEntity golem, int intervalTicks) {
		this.golem = golem;
		this.intervalTicks = intervalTicks;
	}
	
	
	@Override
	public boolean canStart() {
		// TODO Auto-generated method stub
		return !golem.isDormant();
	}
	
	@Override
	public void start() {
		noTargetCounter = 20*12;
	}
	
	@Override
	public void tick() {
		if(!golem.isDormant()) {
			// Set dormant after period without victims
			if(golem.getTarget() == null || !golem.getTarget().isAlive()) {
				noTargetCounter--;
				
				if(noTargetCounter < 1) {
					golem.setDormant(true);
				}
			} else {
				noTargetCounter = 20*12;
			}
		}
	}

}
