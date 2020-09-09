// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.workstation;

import org.terasology.crafting.system.recipe.render.CraftProcessDisplay;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftingStationRecipe {
    /**
     * Inquires if the specified item is a component for that recipe.
     *
     * @param item
     * @return
     */
    boolean hasAsComponent(EntityRef item);

    /**
     * Inquires if the specified item is a tool used in that recipe.
     *
     * @param item
     * @return
     */
    boolean hasAsTool(EntityRef item);

    /**
     * Inquires if the specified fluid is a component in that recipe.
     *
     * @param fluidType
     * @return
     */
    boolean hasFluidAsComponent(String fluidType);

    List<? extends CraftingStationResult> getMatchingRecipeResultsForDisplay(EntityRef station);

    CraftingStationResult getResultByParameters(EntityRef station, List<String> resultParameters);

    interface CraftingStationResult extends CraftProcessDisplay {
        List<String> getResultParameters();

        boolean startCrafting(EntityRef station, int count);

        EntityRef finishCrafting(EntityRef station, int count);
    }
}
