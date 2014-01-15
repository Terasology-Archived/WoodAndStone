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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.was.system.recipe.station.CraftingStationRecipe;

import java.util.Map;

public class UIRecipeDisplay extends UIDisplayContainer {
    private String recipeId;

    public UIRecipeDisplay(String recipeId, SlotBasedInventoryManager inventoryManager, EntityRef entity, CraftingStationRecipe.CraftingStationResult craftingRecipe) {
        this.recipeId = recipeId;
        for (Map.Entry<Integer, Integer> craftingComponents : craftingRecipe.getComponentSlotAndCount().entrySet()) {
            addDisplayElement(new UIPassiveItemDisplay(inventoryManager, inventoryManager.getItemInSlot(entity, craftingComponents.getKey()), craftingComponents.getValue()));
        }
    }
}
