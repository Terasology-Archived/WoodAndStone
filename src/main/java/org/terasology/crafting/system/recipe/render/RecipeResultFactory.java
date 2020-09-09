// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.render;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;

import java.util.List;

public interface RecipeResultFactory {
    int getMaxMultiplier(List<String> parameters);

    EntityRef createResult(List<String> parameters, int multiplier);

    int getCount(List<String> parameters);

    void setupDisplay(List<String> parameters, ItemIcon itemIcon);
}
