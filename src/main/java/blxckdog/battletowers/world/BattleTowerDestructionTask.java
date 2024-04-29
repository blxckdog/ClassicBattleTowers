package blxckdog.battletowers.world;

import blxckdog.battletowers.ClassicBattleTowers;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

public class BattleTowerDestructionTask implements Runnable {

    private static final int INITIAL_DELAY = 20 * 15;
    private static final int PER_FLOOR_DELAY = 20 * 5;
    private static final int MAX_FLOORS = 5;

    private final World world;
    private final BlockPos startPos;
    private final boolean underground;

    private int tickCounter = 0;
    private int floorCounter = 0;


    public BattleTowerDestructionTask(World world, BlockPos startPos) {
        this.startPos = startPos;
        this.world = world;
        underground = false;

        world.playSound(null, startPos, ClassicBattleTowers.SOUND_TOWER_BREAK_START, SoundCategory.HOSTILE, 4f, 1f);
    }


    @Override
    public void run() {
        int centerX = startPos.getX();
        int centerZ = startPos.getZ();
        tickCounter++;

        if (tickCounter % (INITIAL_DELAY + floorCounter * PER_FLOOR_DELAY) == 0) {
            if (!world.isClient) {
            	explodeCircular(centerX, centerZ, 4, 8);
                removeFlyingBlocks();
            }

            floorCounter++;
        }

        if (tickCounter % (INITIAL_DELAY + floorCounter * PER_FLOOR_DELAY + 10) == 0) {
            world.playSound(null, startPos, ClassicBattleTowers.SOUND_TOWER_CRUMBLE, SoundCategory.HOSTILE, 4f, 1f);
        }
    }

    private void explodeCircular(int centerX, int centerZ, int towerRadius, int explosionCount) {
        float angleIncrement = 360 / explosionCount; // The amount to increment the angle each time (this will determine the "tightness" of the spiral)
        
        for(int i=0; i < explosionCount; i++) {
        	float angle = angleIncrement * i;
        	int x = (int) (towerRadius * Math.cos(angle));
            int z = (int) (towerRadius * Math.sin(angle));
            
            world.createExplosion(null, centerX+x, getAdjustedY()-2, centerZ+z, 4f, ExplosionSourceType.MOB);
        }
    }


    private void removeFlyingBlocks() {
        // Remove flying blocks in underground towers???
        int yOffset = underground ? floorCounter * 7 : -floorCounter * 7;

        for (int x = -8; x < 8; x++) {
            for (int z = -8; z < 8; z++) {
                for (int y = 1; y < 9; y++) {
                    BlockPos pos = startPos.add(x, yOffset + y, z);

                    if (!world.isAir(pos)) {
                        world.removeBlock(pos, false);
                    }
                }
            }
        }
    }


    private int getAdjustedY() {
        return underground ? startPos.getY() + floorCounter * 7 : startPos.getY() - floorCounter * 7;
    }


    public boolean isInWorld(World world) {
        return this.world.getRegistryKey().getValue() == world.getRegistryKey().getValue();
    }

    public boolean isFinished() {
        return tickCounter > INITIAL_DELAY + MAX_FLOORS * PER_FLOOR_DELAY;
    }

}
