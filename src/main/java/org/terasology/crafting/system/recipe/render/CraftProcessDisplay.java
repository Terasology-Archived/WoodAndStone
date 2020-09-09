// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.render;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftProcessDisplay {
    List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity);

    boolean isValidForCrafting(EntityRef entity, int multiplier);

    int getMaxMultiplier(EntityRef entity);

    int getResultQuantity();

    void setupResultDisplay(ItemIcon itemIcon);

    long getProcessDuration();
}
