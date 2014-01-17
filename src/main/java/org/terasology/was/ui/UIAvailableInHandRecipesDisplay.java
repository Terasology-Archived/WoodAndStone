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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.was.system.CraftInHandRecipeRegistry;
import org.terasology.was.system.recipe.hand.CraftInHandRecipe;

import javax.vecmath.Vector2f;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UIAvailableInHandRecipesDisplay extends UIDisplayContainer {
    private Multimap<String, String> displayedRecipes = HashMultimap.create();
    private CraftInHandRecipeRegistry registry;
    private EntityRef character;

    public UIAvailableInHandRecipesDisplay(CraftInHandRecipeRegistry registry, EntityRef character) {
        this.registry = registry;
        this.character = character;
        loadRecipes();
    }

    public void update() {
        // TODO: Naive approach by comparing all the possible recipes to those currently displayed
        Multimap<String, String> recipes = HashMultimap.create();
        for (Map.Entry<String, CraftInHandRecipe> craftInHandRecipe : registry.getRecipes().entrySet()) {
            String recipeId = craftInHandRecipe.getKey();
            List<CraftInHandRecipe.CraftInHandResult> results = craftInHandRecipe.getValue().getMatchingRecipeResults(character);
            if (results != null) {
                for (CraftInHandRecipe.CraftInHandResult result : results) {
                    String resultId = result.getResultId();
                    recipes.put(recipeId, resultId);
                }
            }
        }

        if (!recipes.equals(displayedRecipes))
            reloadRecipes();

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
        int rowIndex = 0;

        displayedRecipes.clear();
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        for (Map.Entry<String, CraftInHandRecipe> craftInHandRecipe : registry.getRecipes().entrySet()) {
            String recipeId = craftInHandRecipe.getKey();
            List<CraftInHandRecipe.CraftInHandResult> results = craftInHandRecipe.getValue().getMatchingRecipeResults(character);
            if (results != null) {
                for (CraftInHandRecipe.CraftInHandResult result : results) {
                    String resultId = result.getResultId();
                    displayedRecipes.put(recipeId, resultId);
                    UIRecipeDisplay recipeDisplay = new UIRecipeDisplay(recipeId, resultId, inventoryManager, character, result);
                    recipeDisplay.setPosition(new Vector2f(0, rowIndex * rowHeight));
                    addDisplayElement(recipeDisplay);
                    rowIndex++;
                }
            }
        }
        layout();
    }

    public void dispose() {
        for (UIDisplayElement displayElement : getDisplayElements()) {
            if (displayElement instanceof UIRecipeDisplay) {
                ((UIRecipeDisplay) displayElement).dispose();
            }
        }
    }
}
