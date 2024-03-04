package blxckdog.battletowers.world;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class BattleTowerDestructionManager {

	private static final List<BattleTowerDestructionTask> DESTRUCTION_TASKS = new LinkedList<>();
	
	
	private BattleTowerDestructionManager() {}
	
	
	public static void registerTickEvent() {
		ServerTickEvents.START_WORLD_TICK.register((world) -> {
			Iterator<BattleTowerDestructionTask> iter = DESTRUCTION_TASKS.iterator();
			
			while(iter.hasNext()) {
				BattleTowerDestructionTask task = iter.next();
				
				if(task.isInWorld(world)) {
					task.run();
				}
				
				if(task.isFinished()) {
					iter.remove();
				}
			}
		});
	}
	
	public static boolean registerTask(BattleTowerDestructionTask task) {
		return DESTRUCTION_TASKS.add(task);
	}
}
