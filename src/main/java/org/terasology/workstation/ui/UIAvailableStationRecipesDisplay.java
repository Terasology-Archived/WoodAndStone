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
package org.terasology.workstation.ui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.crafting.ui.CreationCallback;
import org.terasology.crafting.ui.UIRecipeDisplay;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.workstation.event.UserCraftOnStationRequest;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;
import org.terasology.workstation.system.recipe.CraftingStationRecipe;

import javax.vecmath.Vector2f;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UIAvailableStationRecipesDisplay extends UIDisplayContainerScrollable {
    private Multimap<String, String> displayedRecipes = HashMultimap.create();
    private CraftingStationRecipeRegistry registry;
    private String stationType;
    private EntityRef station;
    private int componentFromSlot;
    private int componentSlotCount;
    private int toolFromSlot;
    private int toolSlotCount;

    public UIAvailableStationRecipesDisplay(Vector2f size, CraftingStationRecipeRegistry registry, String stationType, EntityRef station,
                                            int componentFromSlot, int componentSlotCount, int toolFromSlot, int toolSlotCount) {
        super(size);
        this.registry = registry;
        this.stationType = stationType;
        this.station = station;
        this.componentFromSlot = componentFromSlot;
        this.componentSlotCount = componentSlotCount;
        this.toolFromSlot = toolFromSlot;
        this.toolSlotCount = toolSlotCount;
        loadRecipes();
    }

    public void update() {
        // TODO: Naive approach by comparing all the possible recipes to those currently displayed
        Multimap<String, String> recipes = HashMultimap.create();
        for (Map.Entry<String, CraftingStationRecipe> craftInHandRecipe : registry.getCraftingRecipes(stationType).entrySet()) {
            String recipeId = craftInHandRecipe.getKey();
            CraftingStationRecipe recipe = craftInHandRecipe.getValue();
            List<CraftingStationRecipe.CraftingStationResult> results =
                    recipe.getMatchingRecipeResults(station, componentFromSlot, componentSlotCount, toolFromSlot, toolSlotCount);
            if (results != null) {
                for (CraftingStationRecipe.CraftingStationResult result : results) {
                    String resultId = result.getResultId();
                    recipes.put(recipeId, resultId);
                }
            }
        }

        if (!recipes.equals(displayedRecipes)) {
            reloadRecipes();
        }

        super.update();
    }

    private void reloadRecipes() {
        List<UIDisplayElement> uiDisplayElements = new LinkedList<>(getDisplayElements());
        for (UIDisplayElement uiDisplayElement : uiDisplayElements) {
            if (uiDisplayElement instanceof UIRecipeDisplay) {
                UIRecipeDisplay recipeDisplay = (UIRecipeDisplay) uiDisplayElement;
                removeDisplayElement(recipeDisplay);
                recipeDisplay.dispose();
            }
        }

        loadRecipes();
    }

    public void loadRecipes() {
        int rowHeight = 50;
        int rowIndex = -1;

        displayedRecipes.clear();
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        for (Map.Entry<String, CraftingStationRecipe> craftInHandRecipe : registry.getCraftingRecipes(stationType).entrySet()) {
            final String recipeId = craftInHandRecipe.getKey();
            CraftingStationRecipe recipe = craftInHandRecipe.getValue();
            List<CraftingStationRecipe.CraftingStationResult> results =
                    recipe.getMatchingRecipeResults(station, componentFromSlot, componentSlotCount, toolFromSlot, toolSlotCount);
            if (results != null) {
                for (final CraftingStationRecipe.CraftingStationResult result : results) {
                    final String resultId = result.getResultId();
                    displayedRecipes.put(recipeId, resultId);
                    UIRecipeDisplay recipeDisplay = new UIRecipeDisplay(recipeId, resultId, inventoryManager, station, result,
                            new CreationCallback() {
                                @Override
                                public void createOne() {
                                    station.send(new UserCraftOnStationRequest(stationType, recipeId, resultId));
                                }
                            });
                    recipeDisplay.setPosition(new Vector2f(0, 10 + rowIndex * rowHeight));
                    addDisplayElement(recipeDisplay);
                    rowIndex++;
                }
            }
        }
    }

    public void dispose() {
        for (UIDisplayElement displayElement : getDisplayElements()) {
            if (displayElement instanceof UIRecipeDisplay) {
                ((UIRecipeDisplay) displayElement).dispose();
            }
        }
    }
}
