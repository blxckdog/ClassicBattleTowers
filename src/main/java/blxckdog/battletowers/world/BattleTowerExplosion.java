package blxckdog.battletowers.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class BattleTowerExplosion implements Explosion {

    private final ServerWorld world;
    private Vec3d position;

    protected BattleTowerExplosion(ServerWorld world) {
        this.world = world;
        this.position = Vec3d.ZERO;
    }

    public void updatePosition(BlockPos pos) {
        this.position = Vec3d.of(pos);
    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public DestructionType getDestructionType() {
        return DestructionType.DESTROY;
    }

    @Nullable
    @Override
    public LivingEntity getCausingEntity() {
        return null;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return null;
    }

    @Override
    public float getPower() {
        return 8;
    }

    @Override
    public Vec3d getPosition() {
        return position;
    }

    @Override
    public boolean canTriggerBlocks() {
        return false;
    }

    @Override
    public boolean preservesDecorativeEntities() {
        return false;
    }
}
