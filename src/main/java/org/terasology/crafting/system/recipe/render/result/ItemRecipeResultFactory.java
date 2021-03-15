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

import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;

import java.util.List;

public class ItemRecipeResultFactory implements RecipeResultFactory {
    private Prefab prefab;
    private int count;

    public ItemRecipeResultFactory(Prefab prefab, int count) {
        this.prefab = prefab;
        this.count = count;
    }

    @Override
    public int getMaxMultiplier(List<String> parameters) {
        final ItemComponent item = prefab.getComponent(ItemComponent.class);
        if (item.stackId == null || item.stackId.isEmpty()) {
            return 1;
        } else {
            return item.maxStackSize / count;
        }
    }

    @Override
    public EntityRef createResult(List<String> parameters, int multiplier) {
        final EntityRef entity = CoreRegistry.get(EntityManager.class).create(prefab);
        final ItemComponent item = entity.getComponent(ItemComponent.class);
        item.stackCount = (byte) (count * multiplier);
        entity.saveComponent(item);
        return entity;
    }

    @Override
    public int getCount(List<String> parameters) {
        return count;
    }

    @Override
    public void setupDisplay(List<String> parameters, ItemIcon itemIcon) {
        ItemComponent i = prefab.getComponent(ItemComponent.class);

        itemIcon.setIcon(prefab.getComponent(ItemComponent.class).icon);
        DisplayNameComponent displayName = prefab.getComponent(DisplayNameComponent.class);
        if (displayName != null) {
            itemIcon.setTooltip(displayName.name);
        }
    }
}
