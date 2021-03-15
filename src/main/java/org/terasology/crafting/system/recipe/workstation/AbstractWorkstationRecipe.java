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
package org.terasology.crafting.system.recipe.workstation;

import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.module.inventory.ui.ItemIcon;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.heat.HeatUtils;
import org.terasology.math.TeraMath;
import org.terasology.workstation.process.WorkstationInventoryUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractWorkstationRecipe implements CraftingStationRecipe {
    private List<IngredientCraftBehaviour<EntityRef>> ingredientBehaviours = new ArrayList<>();
    private List<IngredientCraftBehaviour<EntityRef>> toolBehaviours = new ArrayList<>();
    private List<IngredientCraftBehaviour<String>> fluidBehaviours = new ArrayList<>();

    private List<IngredientCraftBehaviour<?>> allBehaviours = new ArrayList<>();

    private float requiredHeat;
    private long processingDuration;

    private RecipeResultFactory resultFactory;

    public void addIngredientBehaviour(IngredientCraftBehaviour<EntityRef> behaviour) {
        ingredientBehaviours.add(behaviour);
        allBehaviours.add(behaviour);
    }

    public void addToolBehaviour(IngredientCraftBehaviour<EntityRef> behaviour) {
        toolBehaviours.add(behaviour);
        allBehaviours.add(behaviour);
    }

    public void addFluidBehaviour(IngredientCraftBehaviour<String> behaviour) {
        fluidBehaviours.add(behaviour);
        allBehaviours.add(behaviour);
    }

    public void setResultFactory(RecipeResultFactory resultFactory) {
        this.resultFactory = resultFactory;
    }

    public void setRequiredHeat(float requiredHeat) {
        this.requiredHeat = requiredHeat;
    }

    public void setProcessingDuration(long processingDuration) {
        this.processingDuration = processingDuration;
    }

    @Override
    public boolean hasAsComponent(EntityRef item) {
        for (IngredientCraftBehaviour<EntityRef> ingredientBehaviour : ingredientBehaviours) {
            if (ingredientBehaviour.isValidAnyAmount(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAsTool(EntityRef item) {
        for (IngredientCraftBehaviour<EntityRef> toolBehaviour : toolBehaviours) {
            if (toolBehaviour.isValidAnyAmount(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasFluidAsComponent(String fluidType) {
        for (IngredientCraftBehaviour<String> fluidBehaviour : fluidBehaviours) {
            if (fluidBehaviour.isValidAnyAmount(fluidType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<? extends CraftingStationResult> getMatchingRecipeResultsForDisplay(EntityRef station) {
        List<List<String>> listOfResults = new ArrayList<>();

        for (IngredientCraftBehaviour<?> behaviour : allBehaviours) {
            if (!appendBehaviourMatches(station, listOfResults, behaviour)) {
                return null;
            }
        }

        List<Result> resultList = new LinkedList<>();

        List<List<String>> grouping = createParameterCombinationsFromIndex(listOfResults, 0);
        for (List<String> groupingEntry : grouping) {
            resultList.add(new Result(groupingEntry));
        }

        return resultList;
    }

    private boolean appendBehaviourMatches(EntityRef station, List<List<String>> listOfResults, IngredientCraftBehaviour<?> ingredientBehaviour) {
        List<String> validToCraft = ingredientBehaviour.getValidToCraft(station, 1);
        if (validToCraft.size() == 0) {
            return false;
        }

        listOfResults.add(validToCraft);
        return true;
    }

    private List<List<String>> createParameterCombinationsFromIndex(List<List<String>> listOfResults, int index) {
        List<List<String>> result = new LinkedList<>();

        List<String> parameters = listOfResults.get(index);
        if (index + 1 < listOfResults.size()) {
            List<List<String>> nextParameterCombinations = createParameterCombinationsFromIndex(listOfResults, index + 1);

            // Combination of each parameter for this index and all following
            for (String parameter : parameters) {
                for (List<String> followingParameterCombinations : nextParameterCombinations) {
                    List<String> parameterCombination = new LinkedList<>();
                    parameterCombination.add(parameter);
                    parameterCombination.addAll(followingParameterCombinations);
                    result.add(parameterCombination);
                }
            }
        } else {
            for (String parameter : parameters) {
                List<String> paramCombination = new LinkedList<>();
                paramCombination.add(parameter);
                result.add(paramCombination);
            }
        }

        return result;
    }

    @Override
    public CraftingStationResult getResultByParameters(EntityRef station, List<String> resultParameters) {
        return new Result(resultParameters);
    }

    private class Result implements CraftingStationResult {
        private List<String> parameters;
        private List<CraftIngredientRenderer> renderers;

        public Result(List<String> parameters) {
            if (parameters.size() != allBehaviours.size()) {
                throw new IllegalArgumentException("Invalid definition of the result, parameters: " + parameters.size() + ", allBehaviours: " + allBehaviours.size());
            }
            this.parameters = parameters;
        }

        @Override
        public List<String> getResultParameters() {
            return parameters;
        }

        @Override
        public int getMaxMultiplier(EntityRef entity) {
            int maxMultiplier = resultFactory.getMaxMultiplier(parameters);
            for (int i = 0; i < parameters.size(); i++) {
                maxMultiplier = Math.min(maxMultiplier, allBehaviours.get(i).getMaxMultiplier(entity, parameters.get(i)));
            }
            return maxMultiplier;
        }

        @Override
        public boolean startCrafting(EntityRef station, int count) {
            if (!isValidForCrafting(station, count)) {
                return false;
            }

            int index = 0;
            for (IngredientCraftBehaviour<?> behaviour : allBehaviours) {
                behaviour.processIngredient(station, station, parameters.get(index), count);
                index++;
            }

            return true;
        }

        @Override
        public EntityRef finishCrafting(EntityRef station, int count) {
            return resultFactory.createResult(parameters, count);
        }

        @Override
        public boolean isValidForCrafting(EntityRef station, int count) {
            if (requiredHeat > 0) {
                float heat = HeatUtils.calculateHeatForEntity(station, CoreRegistry.get(BlockEntityRegistry.class));
                if (requiredHeat > heat) {
                    return false;
                }
            }

            int index = 0;
            for (IngredientCraftBehaviour<?> behaviour : allBehaviours) {
                if (!behaviour.isValidToCraft(station, parameters.get(index), count)) {
                    return false;
                }
                index++;
            }

            EntityRef resultItem = resultFactory.createResult(parameters, count);
            try {
                ItemComponent item = resultItem.getComponent(ItemComponent.class);
                if (item.stackCount > item.maxStackSize) {
                    return false;
                }
                for (int slot : WorkstationInventoryUtils.getAssignedSlots(station, "OUTPUT")) {
                    EntityRef itemInResultSlot = InventoryUtils.getItemAt(station, slot);
                    if (InventoryUtils.canStackInto(resultItem, itemInResultSlot)) {
                        return true;
                    }
                }
                return false;
            } finally {
                resultItem.destroy();
            }
        }

        @Override
        public List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                int index = 0;
                for (IngredientCraftBehaviour<?> behaviour : allBehaviours) {
                    final CraftIngredientRenderer renderer = behaviour.getRenderer(entity, parameters.get(index));
                    if (renderer != null) {
                        renderers.add(renderer);
                    }
                    index++;
                }

                if (requiredHeat > 0) {
                    renderers.add(new HeatIngredientRenderer(TeraMath.floorToInt(requiredHeat)));
                }
            }
            return renderers;
        }

        @Override
        public long getProcessDuration() {
            return processingDuration;
        }

        @Override
        public int getResultQuantity() {
            return resultFactory.getCount(parameters);
        }

        @Override
        public void setupResultDisplay(ItemIcon itemIcon) {
            resultFactory.setupDisplay(parameters, itemIcon);
        }
    }
}
