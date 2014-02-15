package org.terasology.fluid.system;

import org.terasology.world.liquid.LiquidType;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface FluidRegistry {
    void registerFluid(String fluidType, FluidRenderer fluidRenderer, LiquidType liquidType);

    FluidRenderer getFluidRenderer(String fluidType);

    String getFluidType(LiquidType liquidType);
}
