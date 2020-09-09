// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.behaviour;

import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface IngredientCraftBehaviour<T> {
    boolean isValidAnyAmount(T ingredient);

    List<String> getValidToCraft(EntityRef entity, int multiplier);

    boolean isValidToCraft(EntityRef entity, String parameter, int multiplier);

    int getMaxMultiplier(EntityRef entity, String parameter);

    CraftIngredientRenderer getRenderer(EntityRef entity, String parameter);

    void processIngredient(EntityRef instigator, EntityRef entity, String parameter, int multiplier);
}
