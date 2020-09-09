// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.render.result;

import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;

import java.util.List;

public class BlockRecipeResultFactory implements RecipeResultFactory {
    private final Block block;
    private final int count;

    protected BlockRecipeResultFactory(int count) {
        this(null, count);
    }

    public BlockRecipeResultFactory(Block block, int count) {
        this.block = block;
        this.count = count;
    }

    @Override
    public int getMaxMultiplier(List<String> parameters) {
        if (getBlock(parameters).isStackable()) {
            return 99 / count;
        } else {
            return 1;
        }
    }

    protected Block getBlock(List<String> parameters) {
        return block;
    }

    @Override
    public EntityRef createResult(List<String> parameters, int multiplier) {
        return new BlockItemFactory(CoreRegistry.get(EntityManager.class)).newInstance(getBlock(parameters).getBlockFamily(), count * multiplier);
    }

    @Override
    public int getCount(List<String> parameters) {
        return count;
    }

    @Override
    public void setupDisplay(List<String> parameters, ItemIcon itemIcon) {
        Block blockToDisplay = getBlock(parameters);
        itemIcon.setMesh(blockToDisplay.getMesh());
        itemIcon.setMeshTexture(Assets.getTexture("engine:terrain").get());
        itemIcon.setTooltip(blockToDisplay.getDisplayName());
    }
}
