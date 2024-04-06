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
                // Reduce damage ???
                explode(centerX, centerZ, 7, floorCounter);
                removeFlyingBlocks();
            }

            floorCounter++;
        }

        if (tickCounter % (INITIAL_DELAY + floorCounter * PER_FLOOR_DELAY + 10) == 0) {
            world.playSound(null, startPos, ClassicBattleTowers.SOUND_TOWER_CRUMBLE, SoundCategory.HOSTILE, 4f, 1f);
        }
    }

    private void explode(int centerX, int centerZ, int towerRadius, int floorCounter) {
        double angle = 0; // The initial angle
        double radius = 0; // The initial radius
        double angleIncrement = Math.PI / 36; // The amount to increment the angle each time (this will determine the "tightness" of the spiral)
        double radiusIncrement = 0.5; // The amount to increment the radius each time (this will determine the "width" of the spiral)

        while (radius <= towerRadius) {
            // Convert polar coordinates to Cartesian coordinates
            int x = (int) (radius * Math.cos(angle));
            int z = (int) (radius * Math.sin(angle));

            // Create the vertical column of explosions at this (x, z) position
            for (int y = 0; y < 7; y++) {
                BlockPos pos = new BlockPos(centerX + x, getAdjustedY() + y, centerZ + z);
                world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 1f, ExplosionSourceType.MOB);
            }

            // Increment the angle and radius for the next iteration
            angle += angleIncrement;
            radius += radiusIncrement;
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
