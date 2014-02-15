package org.terasology.fluid.system;

import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.world.liquid.LiquidType;

import java.awt.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class FluidCommonSystem extends BaseComponentSystem {
    @In
    private FluidRegistry fluidRegistry;

    @Override
    public void preBegin() {
        AssetUri waterTextureUri = TextureUtil.getTextureUriForColor(Color.BLUE);
        fluidRegistry.registerFluid("Fluid:Water", new TextureFluidRenderer(Assets.getTexture(waterTextureUri.toSimpleString())), LiquidType.WATER);
    }
}
