package blxckdog.battletowers.entity.ai;

import java.util.EnumSet;

import blxckdog.battletowers.entity.TowerGolemEntity;
import net.minecraft.entity.ai.goal.Goal;

public class NotDormantGoalProxy extends Goal {

	private final Goal wrapped;
	private final TowerGolemEntity towerGolem;
	
	public NotDormantGoalProxy(TowerGolemEntity golem, Goal wrapped) {
		this.towerGolem = golem;
		this.wrapped = wrapped;
	}
	
	@Override
	public boolean canStart() {
		return towerGolem.isDormant() ? false : wrapped.canStart();
	}
	
	@Override
	public boolean shouldContinue() {
		return towerGolem.isDormant() ? false : wrapped.shouldContinue();
	}
	
	@Override
	public boolean canStop() {
		return wrapped.canStop();
	}
	
	@Override
	public EnumSet<Control> getControls() {
		return wrapped.getControls();
	}
	
	@Override
	public void setControls(EnumSet<Control> controls) {
		wrapped.setControls(controls);
	}
	
	@Override
	public boolean shouldRunEveryTick() {
		return wrapped.shouldRunEveryTick();
	}
	
	@Override
	public void start() {
		wrapped.start();
	}
	
	@Override
	public void stop() {
		wrapped.stop();
	}
	
	@Override
	public void tick() {
		wrapped.tick();
	}
	
	@Override
	public String toString() {
		return wrapped.toString();
	}
	
	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}

}
