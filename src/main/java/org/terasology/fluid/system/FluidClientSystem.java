package org.terasology.fluid.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.fluid.component.FluidContainerItemComponent;
import org.terasology.math.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.ingame.inventory.BeforeInventoryCellRendered;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class FluidClientSystem extends BaseComponentSystem {
    @In
    private FluidRegistry fluidRegistry;

    @ReceiveEvent
    public void renderFluidInInventory(BeforeInventoryCellRendered event, EntityRef item, FluidContainerItemComponent component) {
        String fluidType = component.fluidType;
        if (fluidType != null) {
            Region3i renderRect = component.fluidRenderRect;
            FluidRenderer fluidRenderer = fluidRegistry.getFluidRenderer(fluidType);
            fluidRenderer.renderFluid(event.getCanvas(),
                    Rect2i.createFromMinAndMax(renderRect.min().x, renderRect.min().y, renderRect.max().x, renderRect.max().y));
        }
    }
}
