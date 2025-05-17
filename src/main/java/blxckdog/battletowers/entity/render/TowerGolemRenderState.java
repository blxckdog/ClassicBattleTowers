package blxckdog.battletowers.entity.render;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;

public class TowerGolemRenderState extends BipedEntityRenderState {

    public boolean isDormant;

    protected TowerGolemRenderState() {
        super();
        isDormant = false;
    }

}
