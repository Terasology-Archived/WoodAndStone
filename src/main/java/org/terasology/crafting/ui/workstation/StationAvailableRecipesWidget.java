// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.ui.workstation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.joml.Vector2i;
import org.terasology.crafting.event.CraftingWorkstationProcessRequest;
import org.terasology.crafting.system.CraftingWorkstationProcess;
import org.terasology.crafting.system.recipe.workstation.CraftingStationRecipe;
import org.terasology.crafting.ui.CraftRecipeWidget;
import org.terasology.crafting.ui.CreationCallback;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class StationAvailableRecipesWidget extends CoreWidget {
    private final Set<String> openCategories = new HashSet<>();
    private final Set<String> displayedOpenCategories = new HashSet<>();
    private final Multimap<String, List<String>> availableRecipes = HashMultimap.create();
    private final WorkstationRegistry registry;
    private final ColumnLayout layout;
    private EntityRef station;

    public StationAvailableRecipesWidget() {
        layout = new ColumnLayout();
        layout.setColumns(1);

        registry = CoreRegistry.get(WorkstationRegistry.class);
    }

    public void setStation(EntityRef station) {
        this.station = station;
        reloadRecipes();
    }

    @Override
    public void update(float delta) {
        // TODO: Naive approach by comparing all the possible recipes to those currently displayed
        WorkstationComponent workstation = station.getComponent(WorkstationComponent.class);
        Multimap<String, List<String>> recipes = HashMultimap.create();
        for (WorkstationProcess workstationProcess :
                registry.getWorkstationProcesses(workstation.supportedProcessTypes.keySet())) {
            if (workstationProcess instanceof CraftingWorkstationProcess) {
                CraftingStationRecipe craftingStationRecipe =
                        ((CraftingWorkstationProcess) workstationProcess).getCraftingWorkstationRecipe();
                String recipeId = workstationProcess.getId();
                List<? extends CraftingStationRecipe.CraftingStationResult> results =
                        craftingStationRecipe.getMatchingRecipeResultsForDisplay(station);
                if (results != null) {
                    for (CraftingStationRecipe.CraftingStationResult result : results) {
                        List<String> parameters = result.getResultParameters();
                        recipes.put(recipeId, parameters);
                    }
                }
            }
        }

        if (!openCategories.equals(displayedOpenCategories) || !recipes.equals(availableRecipes)) {
            reloadRecipes();
        }

        layout.update(delta);
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
        availableRecipes.clear();
        displayedOpenCategories.clear();

        displayedOpenCategories.addAll(openCategories);

        Multimap<String, CraftingStationRecipe.CraftingStationResult> withoutCategory = LinkedHashMultimap.create();
        Multimap<String, String> categoryRelationships = TreeMultimap.create(Ordering.natural(), Ordering.natural());
        Set<String> topLevelCategories = new TreeSet<>();

        Map<String, Multimap<String, CraftingStationRecipe.CraftingStationResult>> categoryRecipesMap =
                Maps.newHashMap();

        WorkstationComponent workstation = station.getComponent(WorkstationComponent.class);
        for (WorkstationProcess workstationProcess :
                registry.getWorkstationProcesses(workstation.supportedProcessTypes.keySet())) {
            if (workstationProcess instanceof CraftingWorkstationProcess) {
                String recipeId = workstationProcess.getId();
                List<? extends CraftingStationRecipe.CraftingStationResult> results =
                        ((CraftingWorkstationProcess) workstationProcess).getCraftingWorkstationRecipe().getMatchingRecipeResultsForDisplay(station);
                if (results != null) {
                    for (CraftingStationRecipe.CraftingStationResult result : results) {
                        availableRecipes.put(recipeId, result.getResultParameters());

                        String category = getCategory(recipeId);
                        if (category == null) {
                            withoutCategory.put(recipeId, result);
                        } else {
                            Multimap<String, CraftingStationRecipe.CraftingStationResult> categoryRecipes =
                                    categoryRecipesMap.get(category);
                            if (categoryRecipes == null) {
                                categoryRecipes = LinkedHashMultimap.create();
                                categoryRecipesMap.put(category, categoryRecipes);
                            }
                            categoryRecipes.put(recipeId, result);
                            String topLevel = fillRelationships(categoryRelationships, category);
                            topLevelCategories.add(topLevel);
                        }
                    }
                }
            }
        }

        for (String topLevelCategory : topLevelCategories) {
            int level = 0;

            appendCategory(categoryRelationships, categoryRecipesMap, topLevelCategory, level);
        }

        appendRecipes(0, withoutCategory.entries());
    }

    private void appendCategory(Multimap<String, String> categoryRelationships,
                                Map<String, Multimap<String, CraftingStationRecipe.CraftingStationResult>> categoryRecipesMap,
                                String category, int level) {
        Multimap<String, CraftingStationRecipe.CraftingStationResult> directRecipes = categoryRecipesMap.get(category);
        Collection<String> childCategories = categoryRelationships.get(category);

        int count = 0;
        if (directRecipes != null) {
            count += directRecipes.size();
        }
        count += childCategories.size();

        boolean isOpen = openCategories.contains(category);

        RecipeCategoryWidget categoryWidget = new RecipeCategoryWidget(isOpen, 25 * level, getCategoryName(category),
                count,
                new CategoryToggleCallbackImpl(category));
        layout.addWidget(categoryWidget);

        if (isOpen) {
            for (String childCategory : childCategories) {
                appendCategory(categoryRelationships, categoryRecipesMap, childCategory, level + 1);
            }

            if (directRecipes != null) {
                appendRecipes(level + 1, directRecipes.entries());
            }
        }
    }

    private void appendRecipes(int level,
                               Collection<Map.Entry<String, CraftingStationRecipe.CraftingStationResult>> recipes) {
        for (Map.Entry<String, CraftingStationRecipe.CraftingStationResult> recipeResult : recipes) {
            final String recipeId = recipeResult.getKey();
            CraftingStationRecipe.CraftingStationResult result = recipeResult.getValue();
            final List<String> parameters = result.getResultParameters();
            CraftRecipeWidget recipeDisplay = new CraftRecipeWidget(25 * level, station, result,
                    new CreationCallback() {
                        @Override
                        public void create(int count) {
                            EntityRef player = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                            player.send(new CraftingWorkstationProcessRequest(station, recipeId, parameters, count));
                        }
                    });
            layout.addWidget(recipeDisplay);
        }
    }

    private String fillRelationships(Multimap<String, String> categoryRelationships, String category) {
        String parentCategory = getCategory(category);
        if (parentCategory != null) {
            categoryRelationships.put(parentCategory, category);
            return fillRelationships(categoryRelationships, parentCategory);
        }
        return category;
    }

    private String getCategoryName(String category) {
        if (category.lastIndexOf('|') < 0) {
            return category;
        } else {
            return category.substring(category.lastIndexOf('|') + 1);
        }
    }

    private String getCategory(String recipeId) {
        if (recipeId.lastIndexOf('|') < 0) {
            return null;
        } else {
            return recipeId.substring(0, recipeId.lastIndexOf('|'));
        }
    }

    private final class CategoryToggleCallbackImpl implements CategoryToggleCallback {
        private final String category;

        private CategoryToggleCallbackImpl(String category) {
            this.category = category;
        }

        @Override
        public void categoryToggled() {
            if (openCategories.contains(category)) {
                openCategories.remove(category);
            } else {
                openCategories.add(category);
            }
        }

    }
}
