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

import org.terasology.asset.Assets;
import org.terasology.crafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.heat.HeatUtils;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleWorkstationRecipe implements CraftingStationRecipe {
    private List<IngredientCraftBehaviour<EntityRef, Integer>> ingredientBehaviours = new ArrayList<>();
    private List<IngredientCraftBehaviour<EntityRef, Integer>> toolBehaviours = new ArrayList<>();
    private List<IngredientCraftBehaviour<String, Integer>> fluidBehaviours = new ArrayList<>();

    private float requiredHeat;
    private long processingDuration;

    private String blockResult;
    private String itemResult;
    private byte resultCount;

    public void addIngredient(String type, int count) {
        ingredientBehaviours.add(new ConsumeItemCraftBehaviour(new CraftingStationIngredientPredicate(type), count, new InventorySlotTypeResolver("INPUT")));
    }

    public void addRequiredTool(String toolType, int durability) {
        toolBehaviours.add(new ReduceDurabilityCraftBehaviour(new CraftingStationToolPredicate(toolType), durability, new InventorySlotTypeResolver("TOOL")));
    }

    public void addFluid(String fluidType, float volume) {
        fluidBehaviours.add(new ConsumeFluidBehaviour(fluidType, volume, new InventorySlotTypeResolver("FLUID_INPUT")));
    }

    public void setBlockResult(String block, byte count) {
        blockResult = block;
        resultCount = count;
    }

    public void setItemResult(String item, byte count) {
        itemResult = item;
        resultCount = count;
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
    public List<CraftingStationResult> getMatchingRecipeResults(EntityRef station) {
        if (requiredHeat > 0) {
            float heat = HeatUtils.calculateHeatForEntity(station, CoreRegistry.get(BlockEntityRegistry.class));
            if (requiredHeat > heat) {
                return null;
            }
        }

        return getMatchingRecipeResultsForDisplay(station);
    }

    @Override
    public List<CraftingStationResult> getMatchingRecipeResultsForDisplay(EntityRef station) {
        // TODO: Improve the search to find fragmented ingredients in multiple stacks, and also to find different kinds
        // of items, not just first matching
        List<Integer> resultSlots = new LinkedList<>();
        int maxMultiplier = Integer.MAX_VALUE;

        for (IngredientCraftBehaviour<?, Integer> ingredientBehaviour : ingredientBehaviours) {
            int slotNo = hasIngredient(station, ingredientBehaviour);
            if (slotNo != -1) {
                maxMultiplier = Math.min(maxMultiplier, ingredientBehaviour.getMaxMultiplier(station, slotNo));
                resultSlots.add(slotNo);
            } else {
                return null;
            }
        }

        for (IngredientCraftBehaviour<?, Integer> toolBehaviour : toolBehaviours) {
            int slotNo = hasIngredient(station, toolBehaviour);
            if (slotNo != -1) {
                maxMultiplier = Math.min(maxMultiplier, toolBehaviour.getMaxMultiplier(station, slotNo));
                resultSlots.add(slotNo);
            } else {
                return null;
            }
        }

        for (IngredientCraftBehaviour<String, Integer> fluidBehaviour : fluidBehaviours) {
            int slotNo = hasIngredient(station, fluidBehaviour);
            if (slotNo != -1) {
                maxMultiplier = Math.min(maxMultiplier, fluidBehaviour.getMaxMultiplier(station, slotNo));
                resultSlots.add(slotNo);
            } else {
                return null;
            }
        }

        EntityRef result = createResult(1);
        try {
            ItemComponent resultItem = result.getComponent(ItemComponent.class);
            int maxOutput = TeraMath.floorToInt(1f * resultItem.maxStackSize / resultItem.stackCount);
            maxMultiplier = Math.min(maxMultiplier, maxOutput);
        } finally {
            result.destroy();
        }

        return Collections.<CraftingStationResult>singletonList(new Result(maxMultiplier, resultSlots));
    }

    private int hasIngredient(EntityRef station, IngredientCraftBehaviour<?, Integer> behaviour) {
        List<Integer> slots = behaviour.getValidToCraft(station, 1);
        if (slots.size() > 0) {
            return slots.get(0);
        }

        return -1;
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
        if (itemResult != null) {
            final EntityRef entity = CoreRegistry.get(EntityManager.class).create(itemResult);
            final ItemComponent item = entity.getComponent(ItemComponent.class);
            item.stackCount = (byte) (resultCount * count);
            entity.saveComponent(item);
            return entity;
        } else {
            BlockFamily blockFamily = CoreRegistry.get(BlockManager.class).getBlockFamily(blockResult);
            return new BlockItemFactory(CoreRegistry.get(EntityManager.class)).newInstance(blockFamily, resultCount * count);
        }
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
            }
            return renderers;
        }

        @Override
        public long getProcessDuration() {
            return processingDuration;
        }

        @Override
        public int getResultQuantity() {
            return resultCount;
        }

        @Override
        public Block getResultBlock() {
            if (itemResult == null) {
                return CoreRegistry.get(BlockManager.class).getBlockFamily(blockResult).getArchetypeBlock();
            }
            return null;
        }

        @Override
        public Prefab getResultItem() {
            if (itemResult != null) {
                return Assets.getPrefab(itemResult);
            }
            return null;
        }
    }
}
