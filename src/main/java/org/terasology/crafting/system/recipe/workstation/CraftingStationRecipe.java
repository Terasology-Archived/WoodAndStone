/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.system.recipe.workstation;

import org.terasology.crafting.system.recipe.hand.CraftProcessDisplay;
import org.terasology.entitySystem.entity.EntityRef;

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

    /**
     * Returns all possible recipes (components->result) that can be done processed using this recipe
     * from the components and tools available in the station entity's inventory.
     *
     * @param station
     * @return
     */
    List<CraftingStationResult> getMatchingRecipeResults(EntityRef station);

    CraftingStationResult getResultById(String resultId);

    public interface CraftingStationResult extends CraftProcessDisplay {
        String getResultId();

        /**
         * Processes the crafting of the recipe once. Returns the entity that should be put (or added to) the result slot.
         *
         * @param station
         * @return
         */
        EntityRef craftOne(EntityRef station);

        /**
         * Processes the crafting of the recipe maximum possible times. Returns the entity that should be put (or added to)
         * the result slot.
         *
         * @param station
         * @return
         */
        EntityRef craftMax(EntityRef station);
    }
}
