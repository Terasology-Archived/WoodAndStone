// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.render.result;

import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;

import java.util.List;

public class ItemRecipeResultFactory implements RecipeResultFactory {
    private final Prefab prefab;
    private final int count;

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
