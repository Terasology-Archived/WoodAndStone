/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.system.recipe.render.result;

import org.terasology.asset.Assets;
import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.world.block.Block;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.List;

public class BlockRecipeResultFactory implements RecipeResultFactory {
    private Block block;
    private int count;

    public BlockRecipeResultFactory(Block block, int count) {
        this.block = block;
        this.count = count;
    }

    @Override
    public int getMaxMultiplier(List<String> parameters) {
        if (block.isStackable()) {
            return 99 / count;
        } else {
            return 1;
        }
    }

    @Override
    public EntityRef createResult(List<String> parameters, int multiplier) {
        return new BlockItemFactory(CoreRegistry.get(EntityManager.class)).newInstance(block.getBlockFamily(), count * multiplier);
    }

    @Override
    public int getCount(List<String> parameters) {
        return count;
    }

    @Override
    public void setupDisplay(List<String> parameters, ItemIcon itemIcon) {
        itemIcon.setMesh(block.getMesh());
        itemIcon.setMeshTexture(Assets.getTexture("engine:terrain"));
        itemIcon.setTooltip(block.getDisplayName());
    }
}
