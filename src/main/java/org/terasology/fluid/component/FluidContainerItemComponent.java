package org.terasology.fluid.component;

import org.terasology.entitySystem.Component;
import org.terasology.logic.inventory.ItemDifferentiating;
import org.terasology.math.Region3i;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ItemDifferentiating
public class FluidContainerItemComponent implements Component {
    public String fluidType;
    public float litres;
    public Region3i fluidRenderRect;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FluidContainerItemComponent that = (FluidContainerItemComponent) o;

        if (Float.compare(that.litres, litres) != 0) {
            return false;
        }
        if (fluidRenderRect != null ? !fluidRenderRect.equals(that.fluidRenderRect) : that.fluidRenderRect != null) {
            return false;
        }
        if (fluidType != null ? !fluidType.equals(that.fluidType) : that.fluidType != null) {
            return false;
        }

        return true;
    }
}
