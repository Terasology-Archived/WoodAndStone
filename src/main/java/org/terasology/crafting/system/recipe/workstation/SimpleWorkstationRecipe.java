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
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.ItemSlotIngredientRenderer;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.system.FluidManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleWorkstationRecipe implements CraftingStationRecipe {
    private List<IngredientCraftBehaviour> ingredientBehaviours = new ArrayList<>();
    private List<IngredientCraftBehaviour> toolBehaviours = new ArrayList<>();
    private Map<String, Float> fluidMap = new LinkedHashMap<>();

    private String blockResult;
    private String itemResult;
    private byte resultCount;

    public void addIngredient(String type, int count) {
        ingredientBehaviours.add(new ConsumeItemCraftBehaviour(new CraftingStationIngredientPredicate(type), count));
    }

    public void addRequiredTool(String toolType, int durability) {
        toolBehaviours.add(new ReduceDurabilityCraftBehaviour(new CraftingStationToolPredicate(toolType), durability));
    }

    public void addFluid(String fluidType, float volume) {
        fluidMap.put(fluidType, volume);
    }

    public void setBlockResult(String block, byte count) {
        blockResult = block;
        resultCount = count;
    }

    public void setItemResult(String item, byte count) {
        itemResult = item;
        resultCount = count;
    }

    @Override
    public boolean hasAsComponent(EntityRef item) {
        for (IngredientCraftBehaviour ingredientBehaviour : ingredientBehaviours) {
            if (ingredientBehaviour.isValidAnyAmount(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAsTool(EntityRef item) {
        for (IngredientCraftBehaviour toolBehaviour : toolBehaviours) {
            if (toolBehaviour.isValidAnyAmount(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasFluidAsComponent(String fluidType) {
        return fluidMap.containsKey(fluidType);
    }

    @Override
    public List<CraftingStationResult> getMatchingRecipeResults(EntityRef station) {
        // TODO: Improve the search to find fragmented ingredients in multiple stacks, and also to find different kinds
        // of items, not just first matching
        List<Integer> resultSlots = new LinkedList<>();
        int maxMultiplier = Integer.MAX_VALUE;

        for (IngredientCraftBehaviour ingredientBehaviour : ingredientBehaviours) {
            int slotNo = hasItem(station, ingredientBehaviour);
            if (slotNo != -1) {
                EntityRef item = InventoryUtils.getItemAt(station, slotNo);
                maxMultiplier = Math.min(maxMultiplier, ingredientBehaviour.getMaxMultiplier(item));
                resultSlots.add(slotNo);
                break;
            } else {
                return null;
            }
        }

        for (IngredientCraftBehaviour toolBehaviour : toolBehaviours) {
            int slotNo = hasTool(station, toolBehaviour);
            if (slotNo != -1) {
                EntityRef item = InventoryUtils.getItemAt(station, slotNo);
                maxMultiplier = Math.min(maxMultiplier, toolBehaviour.getMaxMultiplier(item));
                resultSlots.add(slotNo);
                break;
            } else {
                return null;
            }
        }

        for (Map.Entry<String, Float> fluidVolume : fluidMap.entrySet()) {
            int fluidSlotNo = hasFluid(station, fluidVolume.getKey(), fluidVolume.getValue());
            if (fluidSlotNo != -1) {
                FluidInventoryComponent fluidInventory = station.getComponent(FluidInventoryComponent.class);
                FluidComponent fluid = fluidInventory.fluidSlots.get(fluidSlotNo).getComponent(FluidComponent.class);
                maxMultiplier = Math.min(maxMultiplier, TeraMath.floorToInt(fluid.volume / fluidVolume.getValue()));
                resultSlots.add(fluidSlotNo);
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

    private int hasFluid(EntityRef station, String fluidType, Float volume) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(station, "FLUID_INPUT")) {
            if (hasFluidInSlot(station, fluidType, slot, volume)) {
                return slot;
            }
        }

        return -1;
    }

    private int hasTool(EntityRef station, IngredientCraftBehaviour behaviour) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(station, "TOOL")) {
            if (hasItemInSlot(station, slot, behaviour)) {
                return slot;
            }
        }

        return -1;
    }

    private int hasItem(EntityRef station, IngredientCraftBehaviour behaviour) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(station, "INPUT")) {
            if (hasItemInSlot(station, slot, behaviour)) {
                return slot;
            }
        }

        return -1;
    }

    private boolean hasFluidInSlot(EntityRef station, String fluidType, int slot, float volume) {
        FluidInventoryComponent fluidInventory = station.getComponent(FluidInventoryComponent.class);
        FluidComponent fluid = fluidInventory.fluidSlots.get(slot).getComponent(FluidComponent.class);
        return fluid != null && fluid.fluidType.equals(fluidType) && fluid.volume >= volume;
    }

    private boolean hasItemInSlot(EntityRef station, int slot, IngredientCraftBehaviour behaviour) {
        EntityRef item = InventoryUtils.getItemAt(station, slot);
        return behaviour.isValid(item, 1);
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
            fluids = slots.subList(ingredientBehaviours.size() + toolBehaviours.size(), ingredientBehaviours.size() + toolBehaviours.size() + fluidMap.size());
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
        public EntityRef craft(EntityRef station, int count) {
            if (!validateCreation(station, count)) {
                return EntityRef.NULL;
            }

            FluidManager fluidManager = CoreRegistry.get(FluidManager.class);

            int index = 0;
            for (IngredientCraftBehaviour ingredientBehaviour : ingredientBehaviours) {
                EntityRef item = InventoryUtils.getItemAt(station, items.get(index));
                ingredientBehaviour.processIngredient(station, station, item, count);
                index++;
            }

            index = 0;
            for (IngredientCraftBehaviour toolBehaviour : toolBehaviours) {
                final EntityRef tool = InventoryUtils.getItemAt(station, tools.get(index));
                toolBehaviour.processIngredient(station, station, tool, count);
                index++;
            }

            index = 0;
            for (Map.Entry<String, Float> fluidVolume : fluidMap.entrySet()) {
                fluidManager.removeFluid(station, station, fluids.get(index), fluidVolume.getKey(), fluidVolume.getValue() * count);
                index++;
            }

            return createResult(count);
        }

        private boolean validateCreation(EntityRef station, int count) {
            int index = 0;
            for (IngredientCraftBehaviour ingredientBehaviour : ingredientBehaviours) {
                if (!ingredientBehaviour.isValid(InventoryUtils.getItemAt(station, items.get(index)), count)) {
                    return false;
                }
                index++;
            }

            index = 0;
            for (IngredientCraftBehaviour toolBehaviour : toolBehaviours) {
                if (!toolBehaviour.isValid(InventoryUtils.getItemAt(station, tools.get(index)), count)) {
                    return false;
                }
                index++;
            }

            index = 0;
            for (Map.Entry<String, Float> fluidVolume : fluidMap.entrySet()) {
                if (!hasFluidInSlot(station, fluidVolume.getKey(), fluids.get(index), fluidVolume.getValue() * count)) {
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
        public List<CraftIngredientRenderer> getIngredients(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                int index = 0;
                for (final IngredientCraftBehaviour ingredientBehaviour : ingredientBehaviours) {
                    renderers.add(new ItemSlotIngredientRenderer(entity, items.get(index), ingredientBehaviour.getCountBasedOnMultiplier()));
                    index++;
                }
            }
            return renderers;
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
