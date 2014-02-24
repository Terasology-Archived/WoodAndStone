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
package org.terasology.crafting.system.recipe.workstation.result;

import org.terasology.crafting.system.recipe.workstation.RecipeResultFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;

public class ItemRecipeResultFactory implements RecipeResultFactory {
    private Prefab prefab;
    private int count;

    public ItemRecipeResultFactory(Prefab prefab, int count) {
        this.prefab = prefab;
        this.count = count;
    }

    @Override
    public EntityRef createResult(int multiplier) {
        final EntityRef entity = CoreRegistry.get(EntityManager.class).create(prefab);
        final ItemComponent item = entity.getComponent(ItemComponent.class);
        item.stackCount = (byte) (count * multiplier);
        entity.saveComponent(item);
        return entity;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setupDisplay(ItemIcon itemIcon) {
        itemIcon.setIcon(prefab.getComponent(ItemComponent.class).icon);
        DisplayNameComponent displayName = prefab.getComponent(DisplayNameComponent.class);
        if (displayName != null) {
            itemIcon.setTooltip(displayName.name);
        }
    }
}
