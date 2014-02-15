package org.terasology.fluid.system;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;
import org.terasology.world.liquid.LiquidType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = {FluidRegistry.class})
public class FluidRegistryImpl extends BaseComponentSystem implements FluidRegistry {
    private Map<String, FluidRenderer> fluidRenderers = new HashMap<>();
    private Map<LiquidType, String> liquidMapping = new HashMap<>();

    @Override
    public void registerFluid(String fluidType, FluidRenderer fluidRenderer, LiquidType liquidType) {
        fluidRenderers.put(fluidType, fluidRenderer);
        if (liquidType != null) {
            liquidMapping.put(liquidType, fluidType);
        }
    }

    @Override
    public String getFluidType(LiquidType liquidType) {
        return liquidMapping.get(liquidType);
    }

    @Override
    public FluidRenderer getFluidRenderer(String fluidType) {
        return fluidRenderers.get(fluidType);
    }
}
