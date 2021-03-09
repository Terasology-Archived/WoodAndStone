/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.ui.hand;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joml.Vector2i;
import org.terasology.crafting.event.UserCraftInHandRequest;
import org.terasology.crafting.system.CraftInHandRecipeRegistry;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.crafting.ui.CraftRecipeWidget;
import org.terasology.crafting.ui.CreationCallback;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.ColumnLayout;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftInHandAvailableRecipesWidget extends CoreWidget {
    private Multimap<String, List<String>> displayedRecipes = HashMultimap.create();
    private CraftInHandRecipeRegistry registry;
    private EntityRef character;

    private ColumnLayout layout;

    public CraftInHandAvailableRecipesWidget() {
        layout = new ColumnLayout();
        layout.setColumns(1);

        registry = CoreRegistry.get(CraftInHandRecipeRegistry.class);
        character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
    }

    @Override
    public void update(float delta) {
        // TODO: Naive approach by comparing all the possible recipes to those currently displayed
        Multimap<String, List<String>> recipes = HashMultimap.create();
        for (Map.Entry<String, CraftInHandRecipe> craftInHandRecipe : registry.getRecipes().entrySet()) {
            String recipeId = craftInHandRecipe.getKey();
            List<CraftInHandRecipe.CraftInHandResult> results = craftInHandRecipe.getValue().getMatchingRecipeResults(character);
            if (results != null) {
                for (CraftInHandRecipe.CraftInHandResult result : results) {
                    List<String> parameters = result.getParameters();
                    recipes.put(recipeId, parameters);
                }
            }
        }

        if (!recipes.equals(displayedRecipes)) {
            reloadRecipes();
        }
    }

    private void reloadRecipes() {
        Iterator<UIWidget> oldWidgets = layout.iterator();
        while (oldWidgets.hasNext()) {
            oldWidgets.next();
            oldWidgets.remove();
        }

        loadRecipes();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawWidget(layout);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return layout.getPreferredContentSize(canvas, sizeHint);
    }

    public void loadRecipes() {
        displayedRecipes.clear();
        for (Map.Entry<String, CraftInHandRecipe> craftInHandRecipe : registry.getRecipes().entrySet()) {
            final String recipeId = craftInHandRecipe.getKey();
            List<CraftInHandRecipe.CraftInHandResult> results = craftInHandRecipe.getValue().getMatchingRecipeResults(character);
            if (results != null) {
                for (CraftInHandRecipe.CraftInHandResult result : results) {
                    final List<String> parameters = result.getParameters();
                    displayedRecipes.put(recipeId, parameters);
                    CraftRecipeWidget recipeDisplay = new CraftRecipeWidget(0, character, result,
                            new CreationCallback() {
                                @Override
                                public void create(int count) {
                                    character.send(new UserCraftInHandRequest(recipeId, parameters, count));
                                }
                            });
                    layout.addWidget(recipeDisplay);
                }
            }
        }
    }
}
