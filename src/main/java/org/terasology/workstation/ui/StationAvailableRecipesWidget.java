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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.terasology.crafting.ui.CraftRecipeWidget;
import org.terasology.crafting.ui.CreationCallback;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.workstation.event.UserCraftOnStationRequest;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;
import org.terasology.workstation.system.recipe.CraftingStationRecipe;

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
    private Set<String> openCategories = new HashSet<>();
    private Set<String> displayedOpenCategories = new HashSet<>();
    private Multimap<String, String> availableRecipes = HashMultimap.create();
    private CraftingStationRecipeRegistry registry;
    private String stationType;

    private EntityRef station;
    private int componentFromSlot;
    private int componentSlotCount;
    private int toolFromSlot;
    private int toolSlotCount;

    private ColumnLayout layout;

    public StationAvailableRecipesWidget() {
        layout = new ColumnLayout();
        layout.setColumns(1);

        registry = CoreRegistry.get(CraftingStationRecipeRegistry.class);
    }

    public void setStation(EntityRef station) {
        this.station = station;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public void setComponentFromSlot(int componentFromSlot) {
        this.componentFromSlot = componentFromSlot;
    }

    public void setComponentSlotCount(int componentSlotCount) {
        this.componentSlotCount = componentSlotCount;
    }

    public void setToolFromSlot(int toolFromSlot) {
        this.toolFromSlot = toolFromSlot;
    }

    public void setToolSlotCount(int toolSlotCount) {
        this.toolSlotCount = toolSlotCount;
    }

    @Override
    public void update(float delta) {
        // TODO: Naive approach by comparing all the possible recipes to those currently displayed
        Multimap<String, String> recipes = HashMultimap.create();
        for (Map.Entry<String, CraftingStationRecipe> craftingStationRecipe : registry.getCraftingRecipes(stationType).entrySet()) {
            String recipeId = craftingStationRecipe.getKey();
            List<CraftingStationRecipe.CraftingStationResult> results = craftingStationRecipe.getValue().getMatchingRecipeResults(station,
                    componentFromSlot, componentSlotCount, toolFromSlot, toolSlotCount);
            if (results != null) {
                for (CraftingStationRecipe.CraftingStationResult result : results) {
                    String resultId = result.getResultId();
                    recipes.put(recipeId, resultId);
                }
            }
        }

        if (!openCategories.equals(displayedOpenCategories) || !recipes.equals(availableRecipes)) {
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
        availableRecipes.clear();
        displayedOpenCategories.clear();

        displayedOpenCategories.addAll(openCategories);

        Multimap<String, CraftingStationRecipe.CraftingStationResult> withoutCategory = LinkedHashMultimap.create();
        Multimap<String, String> categoryRelationships = TreeMultimap.create(Ordering.natural(), Ordering.natural());
        Set<String> topLevelCategories = new TreeSet<>();

        Map<String, Multimap<String, CraftingStationRecipe.CraftingStationResult>> categoryRecipesMap =
                Maps.newHashMap();

        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        for (Map.Entry<String, CraftingStationRecipe> craftInHandRecipe : registry.getCraftingRecipes(stationType).entrySet()) {
            final String recipeId = craftInHandRecipe.getKey();
            List<CraftingStationRecipe.CraftingStationResult> results = craftInHandRecipe.getValue().getMatchingRecipeResults(station,
                    componentFromSlot, componentSlotCount, toolFromSlot, toolSlotCount);
            if (results != null) {
                for (CraftingStationRecipe.CraftingStationResult result : results) {
                    availableRecipes.put(recipeId, result.getResultId());

                    String category = getCategory(recipeId);
                    if (category == null) {
                        withoutCategory.put(recipeId, result);
                    } else {
                        Multimap<String, CraftingStationRecipe.CraftingStationResult> categoryRecipes = categoryRecipesMap.get(category);
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

        for (String topLevelCategory : topLevelCategories) {
            int level = 0;

            appendCategory(categoryRelationships, categoryRecipesMap, inventoryManager, topLevelCategory, level);
        }

        appendRecipes(inventoryManager, 0, withoutCategory.entries());


//        String lastCategory = null;
//        for (Map.Entry<String, CraftingStationRecipe.CraftingStationResult> recipeResult : resultsToDisplay.entries()) {
//            final String recipeId = recipeResult.getKey();
//            final String category = getCategory(recipeId);
//            int categoryLevel = getCategoryLevel(category);
//            if (category != null && !category.equals(lastCategory)) {
//                String categoryName = getCategoryName(category);
//                RecipeCategoryWidget categoryWidget = new RecipeCategoryWidget(10 * categoryLevel, categoryName, 1,
//                        new CategoryToggleCallback() {
//                            @Override
//                            public void categoryToggled() {
//                                if (openCategories.contains(category)) {
//                                    openCategories.remove(category);
//                                } else {
//                                    openCategories.add(category);
//                                }
//                            }
//                        });
//                layout.addWidget(categoryWidget);
//                lastCategory = category;
//            }
//            CraftingStationRecipe.CraftingStationResult result = recipeResult.getValue();
//            final String resultId = result.getResultId();
//            availableRecipes.put(recipeId, resultId);
//            if (category == null || openCategories.contains(category)) {
//                CraftRecipeWidget recipeDisplay = new CraftRecipeWidget(10 * categoryLevel, inventoryManager, station, result,
//                        new CreationCallback() {
//                            @Override
//                            public void createOne() {
//                                station.send(new UserCraftOnStationRequest(stationType, recipeId, resultId));
//                            }
//                        });
//                layout.addWidget(recipeDisplay);
//            }
//        }
    }

    private void appendCategory(Multimap<String, String> categoryRelationships, Map<String, Multimap<String, CraftingStationRecipe.CraftingStationResult>> categoryRecipesMap, InventoryManager inventoryManager, String topLevelCategory, int level) {
        Multimap<String, CraftingStationRecipe.CraftingStationResult> directRecipes = categoryRecipesMap.get(topLevelCategory);
        Collection<String> childCategories = categoryRelationships.get(topLevelCategory);

        int count = 0;
        if (directRecipes != null) {
            count += directRecipes.size();
        }
        count += childCategories.size();

        boolean isOpen = openCategories.contains(topLevelCategory);

        RecipeCategoryWidget categoryWidget = new RecipeCategoryWidget(isOpen, 15 * level, getCategoryName(topLevelCategory), count,
                new CategoryToggleCallbackImpl(topLevelCategory));
        layout.addWidget(categoryWidget);

        if (isOpen) {
            for (String childCategory : childCategories) {
                appendCategory(categoryRelationships, categoryRecipesMap, inventoryManager, childCategory, level + 1);
            }

            appendRecipes(inventoryManager, level + 1, directRecipes.entries());
        }
    }

    private void appendRecipes(InventoryManager inventoryManager, int level, Collection<Map.Entry<String, CraftingStationRecipe.CraftingStationResult>> recipes) {
        for (Map.Entry<String, CraftingStationRecipe.CraftingStationResult> recipeResult : recipes) {
            final String recipeId = recipeResult.getKey();
            CraftingStationRecipe.CraftingStationResult result = recipeResult.getValue();
            final String resultId = result.getResultId();
            CraftRecipeWidget recipeDisplay = new CraftRecipeWidget(15 * level, inventoryManager, station, result,
                    new CreationCallback() {
                        @Override
                        public void createOne() {
                            station.send(new UserCraftOnStationRequest(stationType, recipeId, resultId));
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
        private String category;

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