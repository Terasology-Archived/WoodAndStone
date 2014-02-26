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

import org.terasology.crafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.heat.HeatUtils;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.world.BlockEntityRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class DefaultWorkstationRecipe implements CraftingStationRecipe {
    private List<IngredientCraftBehaviour<EntityRef, Integer>> ingredientBehaviours = new ArrayList<>();
    private List<IngredientCraftBehaviour<EntityRef, Integer>> toolBehaviours = new ArrayList<>();
    private List<IngredientCraftBehaviour<String, Integer>> fluidBehaviours = new ArrayList<>();

    private float requiredHeat;
    private long processingDuration;

    private RecipeResultFactory resultFactory;

    public void addIngredient(String type, int count) {
        ingredientBehaviours.add(new ConsumeItemCraftBehaviour(new CraftingStationIngredientPredicate(type), count, new InventorySlotTypeResolver("INPUT")));
    }

    public void addRequiredTool(String toolType, int durability) {
        toolBehaviours.add(new ReduceDurabilityCraftBehaviour(new CraftingStationToolPredicate(toolType), durability, new InventorySlotTypeResolver("TOOL")));
    }

    public void addFluid(String fluidType, float volume) {
        fluidBehaviours.add(new ConsumeFluidBehaviour(fluidType, volume, new InventorySlotTypeResolver("FLUID_INPUT")));
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
        for (IngredientCraftBehaviour<EntityRef, ?> ingredientBehaviour : ingredientBehaviours) {
            if (ingredientBehaviour.isValidAnyAmount(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAsTool(EntityRef item) {
        for (IngredientCraftBehaviour<EntityRef, ?> toolBehaviour : toolBehaviours) {
            if (toolBehaviour.isValidAnyAmount(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasFluidAsComponent(String fluidType) {
        for (IngredientCraftBehaviour<String, Integer> fluidBehaviour : fluidBehaviours) {
            if (fluidBehaviour.isValidAnyAmount(fluidType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<? extends CraftingStationResult> getMatchingRecipeResults(EntityRef station) {
        if (requiredHeat > 0) {
            float heat = HeatUtils.calculateHeatForEntity(station, CoreRegistry.get(BlockEntityRegistry.class));
            if (requiredHeat > heat) {
                return null;
            }
        }

        return getMatchingRecipeResultsForDisplay(station);
    }

    @Override
    public List<? extends CraftingStationResult> getMatchingRecipeResultsForDisplay(EntityRef station) {
        List<Map<Integer, Integer>> listOfResults = new ArrayList<>();

        for (IngredientCraftBehaviour<?, Integer> ingredientBehaviour : ingredientBehaviours) {
            if (!appendBehaviourMatches(station, listOfResults, ingredientBehaviour)) {
                return null;
            }
        }

        for (IngredientCraftBehaviour<?, Integer> toolBehaviour : toolBehaviours) {
            if (!appendBehaviourMatches(station, listOfResults, toolBehaviour)) {
                return null;
            }
        }

        for (IngredientCraftBehaviour<?, Integer> fluidBehaviour : fluidBehaviours) {
            if (!appendBehaviourMatches(station, listOfResults, fluidBehaviour)) {
                return null;
            }
        }

        int maxResultMultiplier = resultFactory.getMaxMultiplier();

        List<Result> resultList = new LinkedList<>();

        Map<List<Integer>, Integer> grouping = createGrouping(listOfResults, 0);
        for (Map.Entry<List<Integer>, Integer> groupingEntry : grouping.entrySet()) {
            int maxMultiplier = Math.min(maxResultMultiplier, groupingEntry.getValue());
            resultList.add(new Result(maxMultiplier, groupingEntry.getKey()));
        }

        return resultList;
    }

    private boolean appendBehaviourMatches(EntityRef station, List<Map<Integer, Integer>> listOfResults, IngredientCraftBehaviour<?, Integer> ingredientBehaviour) {
        List<Integer> validToCraft = ingredientBehaviour.getValidToCraft(station, 1);
        if (validToCraft.size() == 0) {
            return false;
        }

        Map<Integer, Integer> slotToMaxMultiplier = new LinkedHashMap<>();
        for (int slot : validToCraft) {
            int maxMultiplier = ingredientBehaviour.getMaxMultiplier(station, slot);
            slotToMaxMultiplier.put(slot, maxMultiplier);
        }

        listOfResults.add(slotToMaxMultiplier);
        return true;
    }

    private Map<List<Integer>, Integer> createGrouping(List<Map<Integer, Integer>> listOfResults, int index) {
        Map<List<Integer>, Integer> result = new LinkedHashMap<>();

        Map<Integer, Integer> slotToMax = listOfResults.get(index);
        if (index + 1 < listOfResults.size()) {
            for (Map.Entry<Integer, Integer> slotToMaxEntry : slotToMax.entrySet()) {
                if (index + 1 < listOfResults.size()) {
                    Map<List<Integer>, Integer> nextGrouping = createGrouping(listOfResults, index + 1);
                    for (Map.Entry<List<Integer>, Integer> nextSlotToMax : nextGrouping.entrySet()) {
                        List<Integer> slots = new LinkedList<>();
                        slots.add(slotToMaxEntry.getKey());
                        slots.addAll(nextSlotToMax.getKey());
                        int nextMax = Math.min(slotToMaxEntry.getValue(), nextSlotToMax.getValue());
                        result.put(slots, nextMax);
                    }
                }
            }
        } else {
            for (Map.Entry<Integer, Integer> slotToMaxEntry : slotToMax.entrySet()) {
                result.put(Collections.singletonList(slotToMaxEntry.getKey()), slotToMaxEntry.getValue());
            }
        }

        return result;
    }

    @Override
    public CraftingStationResult getResultById(String resultId) {
        String[] split = resultId.split("\\|");
        List<Integer> result = new LinkedList<>();
        int maxMultiplier = 0;
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                maxMultiplier = Integer.parseInt(split[i]);
            } else {
                result.add(Integer.parseInt(split[i]));
            }
        }

        return new Result(maxMultiplier, result);
    }

    private EntityRef createResult(int count) {
        return resultFactory.createResult(count);
    }

    private class Result implements CraftingStationResult {
        private int maxMultiplier;
        private List<Integer> items;
        private List<Integer> tools;
        private List<Integer> fluids;
        private List<CraftIngredientRenderer> renderers;

        public Result(int maxMultiplier, List<Integer> slots) {
            this.maxMultiplier = maxMultiplier;
            items = slots.subList(0, ingredientBehaviours.size());
            tools = slots.subList(ingredientBehaviours.size(), ingredientBehaviours.size() + toolBehaviours.size());
            fluids = slots.subList(ingredientBehaviours.size() + toolBehaviours.size(), ingredientBehaviours.size() + toolBehaviours.size() + fluidBehaviours.size());
        }

        @Override
        public String getResultId() {
            StringBuilder sb = new StringBuilder();
            sb.append(maxMultiplier).append("|");
            for (Integer item : items) {
                sb.append(item).append("|");
            }
            for (Integer tool : tools) {
                sb.append(tool).append("|");
            }
            for (Integer fluid : fluids) {
                sb.append(fluid).append("|");
            }

            return sb.replace(sb.length() - 1, sb.length() + 1, "").toString();
        }

        @Override
        public int getMaxMultiplier() {
            return maxMultiplier;
        }

        @Override
        public boolean startCrafting(EntityRef station, int count) {
            if (!isValidForCrafting(station, count)) {
                return false;
            }

            int index = 0;
            for (IngredientCraftBehaviour<?, Integer> ingredientBehaviour : ingredientBehaviours) {
                ingredientBehaviour.processIngredient(station, station, items.get(index), count);
                index++;
            }

            index = 0;
            for (IngredientCraftBehaviour<?, Integer> toolBehaviour : toolBehaviours) {
                toolBehaviour.processIngredient(station, station, tools.get(index), count);
                index++;
            }

            index = 0;
            for (IngredientCraftBehaviour<String, Integer> fluidBehaviour : fluidBehaviours) {
                fluidBehaviour.processIngredient(station, station, fluids.get(index), count);
                index++;
            }

            return true;
        }

        @Override
        public EntityRef finishCrafting(EntityRef station, int count) {
            return createResult(count);
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
            for (IngredientCraftBehaviour<?, Integer> ingredientBehaviour : ingredientBehaviours) {
                if (!ingredientBehaviour.isValidToCraft(station, items.get(index), count)) {
                    return false;
                }
                index++;
            }

            index = 0;
            for (IngredientCraftBehaviour<?, Integer> toolBehaviour : toolBehaviours) {
                if (!toolBehaviour.isValidToCraft(station, tools.get(index), count)) {
                    return false;
                }
                index++;
            }

            index = 0;
            for (IngredientCraftBehaviour<String, Integer> fluidBehaviour : fluidBehaviours) {
                if (!fluidBehaviour.isValidToCraft(station, fluids.get(index), count)) {
                    return false;
                }
                index++;
            }

            EntityRef resultItem = createResult(count);
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
                for (IngredientCraftBehaviour<EntityRef, Integer> toolBehaviour : toolBehaviours) {
                    renderers.add(toolBehaviour.getRenderer(entity, tools.get(index)));
                    index++;
                }
                index = 0;
                for (IngredientCraftBehaviour<?, Integer> ingredientBehaviour : ingredientBehaviours) {
                    renderers.add(ingredientBehaviour.getRenderer(entity, items.get(index)));
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
            return resultFactory.getCount();
        }

        @Override
        public void setupResultDisplay(ItemIcon itemIcon) {
            resultFactory.setupDisplay(itemIcon);
        }
    }
}
