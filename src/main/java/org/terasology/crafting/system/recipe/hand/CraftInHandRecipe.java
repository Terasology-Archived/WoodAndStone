// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.hand;

import org.terasology.crafting.system.recipe.render.CraftProcessDisplay;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftInHandRecipe {
    List<CraftInHandResult> getMatchingRecipeResults(EntityRef character);

    CraftInHandResult getResultByParameters(List<String> parameters);

    interface CraftInHandResult extends CraftProcessDisplay {
        List<String> getParameters();

        EntityRef craft(EntityRef character, int count);
    }
}
