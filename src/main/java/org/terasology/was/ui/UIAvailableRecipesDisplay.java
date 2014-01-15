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
package org.terasology.was.ui;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.was.component.CraftingStationComponent;
import org.terasology.was.system.CraftingStationRecipeRegistry;
import org.terasology.was.system.recipe.station.CraftingStationRecipe;

import java.util.List;
import java.util.Map;

public class UIAvailableRecipesDisplay extends UIDisplayContainer {
    public UIAvailableRecipesDisplay(CraftingStationRecipeRegistry recipeRegistry, EntityRef station, int componentFromSlot, int componentSlotCount, int toolFromSlot, int toolSlotCount) {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);

        final Map<String, CraftingStationRecipe> recipes = recipeRegistry.getRecipesForStation(station.getComponent(CraftingStationComponent.class).type);
        for (Map.Entry<String, CraftingStationRecipe> recipe : recipes.entrySet()) {
            final List<CraftingStationRecipe.CraftingStationResult> matchingRecipes = recipe.getValue().getMatchingRecipeResults(station, componentFromSlot, componentSlotCount, toolFromSlot, toolSlotCount);
            if (matchingRecipes != null) {
                for (CraftingStationRecipe.CraftingStationResult matchingRecipe : matchingRecipes) {
                    addDisplayElement(new UIRecipeDisplay(recipe.getKey(), inventoryManager, station, matchingRecipe));
                }
            }
        }
    }
}
