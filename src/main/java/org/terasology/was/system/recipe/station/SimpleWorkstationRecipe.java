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
package org.terasology.was.system.recipe.station;

import org.terasology.durability.DurabilityComponent;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.was.component.CraftingStationIngredientComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.BlockDamageComponent;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleWorkstationRecipe implements CraftingStationRecipe {
    private Map<String, Integer> ingredientsMap = new LinkedHashMap<>();
    private Map<String, Integer> toolsMap = new LinkedHashMap<>();

    private String blockResult;
    private String itemResult;

    public void addIngredient(String type, int count) {
        ingredientsMap.put(type, count);
    }

    public void addRequiredTool(String toolType, int durability) {
        toolsMap.put(toolType, durability);
    }

    public void setBlockResult(String blockResult) {
        this.blockResult = blockResult;
    }

    public void setItemResult(String itemResult) {
        this.itemResult = itemResult;
    }

    @Override
    public boolean hasAsComponent(EntityRef itemEntity) {
        CraftingStationIngredientComponent ingredient = itemEntity.getComponent(CraftingStationIngredientComponent.class);
        return ingredient != null && ingredientsMap.containsKey(ingredient.type);
    }

    @Override
    public boolean hasAsTool(EntityRef itemEntity) {
        ItemComponent component = itemEntity.getComponent(ItemComponent.class);
        if (component == null)
            return false;
        BlockDamageComponent blockDamage = component.damageType.getComponent(BlockDamageComponent.class);
        if (blockDamage == null)
            return false;
        for (String tool : toolsMap.keySet()) {
            if (blockDamage.materialDamageMultiplier.containsKey(tool))
                return true;
        }
        return false;
    }

    @Override
    public List<CraftingStationResult> getMatchingRecipeResults(EntityRef station, int componentFromSlot, int componentSlotCount, int toolFromSlot, int toolSlotCount) {
        // TODO: Improve the search to find fragmented ingredients in multiple stacks, and also to find different kinds
        // of items, not just first matching
        List<Integer> resultSlots = new LinkedList<>();
        for (Map.Entry<String, Integer> ingredientCount : ingredientsMap.entrySet()) {
            int slotNo = hasItem(station, ingredientCount.getKey(), ingredientCount.getValue(), componentFromSlot, componentFromSlot + componentSlotCount);
            if (slotNo != -1)
                resultSlots.add(slotNo);
            else
                return null;
        }

        for (Map.Entry<String, Integer> toolDurability : toolsMap.entrySet()) {
            int slotNo = hasTool(station, toolDurability.getKey(), toolDurability.getValue(), toolFromSlot, toolFromSlot + toolSlotCount);
            if (slotNo != -1)
                resultSlots.add(slotNo);
            else
                return null;
        }

        return Collections.<CraftingStationResult>singletonList(new Result(resultSlots));
    }

    private int hasTool(EntityRef station, String toolType, int durability, int fromSlot, int toSlot) {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        for (int i = fromSlot; i < toSlot; i++) {
            if (hasToolInSlot(station, toolType, inventoryManager, i, durability))
                return i;
        }

        return -1;
    }

    private int hasItem(EntityRef character, String itemType, int count, int fromSlot, int toSlot) {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        for (int i = fromSlot; i < toSlot; i++) {
            if (hasItemInSlot(character, itemType, inventoryManager, i, count))
                return i;
        }

        return -1;
    }

    private boolean hasItemInSlot(EntityRef character, String itemType, SlotBasedInventoryManager inventoryManager, int slot, int count) {
        EntityRef item = inventoryManager.getItemInSlot(character, slot);
        CraftingStationIngredientComponent component = item.getComponent(CraftingStationIngredientComponent.class);
        if (component != null && component.type.equals(itemType) && item.getComponent(ItemComponent.class).stackCount >= count)
            return true;
        return false;
    }

    private boolean hasToolInSlot(EntityRef character, String toolType, SlotBasedInventoryManager inventoryManager, int slot, int durability) {
        EntityRef item = inventoryManager.getItemInSlot(character, slot);
        ItemComponent component = item.getComponent(ItemComponent.class);
        if (component == null)
            return false;

        BlockDamageComponent blockDamage = component.damageType.getComponent(BlockDamageComponent.class);

        return blockDamage != null && blockDamage.materialDamageMultiplier.containsKey(toolType)
                && item.hasComponent(DurabilityComponent.class) && item.getComponent(DurabilityComponent.class).durability >= durability;
    }

    @Override
    public CraftingStationResult getResultById(String resultId) {
        String[] split = resultId.split("\\|");
        List<Integer> result = new LinkedList<>();
        for (String s : split) {
            result.add(Integer.parseInt(s));
        }

        return new Result(result);
    }

    private class Result implements CraftingStationResult {
        private List<Integer> items;
        private List<Integer> tools;

        public Result(List<Integer> slots) {
            items = slots.subList(0, ingredientsMap.size());
            tools = slots.subList(ingredientsMap.size(), toolsMap.size());
        }

        @Override
        public String getResultId() {
            StringBuilder sb = new StringBuilder();
            for (Integer item : items) {
                sb.append(item).append("|");
            }
            for (Integer tool : tools) {
                sb.append(tool).append("|");
            }

            return sb.replace(sb.length() - 1, sb.length() + 1, "").toString();
        }

        @Override
        public EntityRef craftOne(EntityRef stationEntity, int componentFromSlot, int componentSlotCount, int toolFromSlot, int toolSlotCount) {
            return null;
        }

        @Override
        public EntityRef craftMax(EntityRef stationEntity, int componentFromSlot, int componentSlotCount, int toolFromSlot, int toolSlotCount, int resultSlot) {
            return null;
        }

        @Override
        public Map<Integer, Integer> getComponentSlotAndCount() {
            Map<Integer, Integer> result = new LinkedHashMap<>();
            int index = 0;
            for (Integer count : ingredientsMap.values()) {
                result.put(items.get(index), count);
                index++;
            }

            return result;
        }

        @Override
        public EntityRef createResultItemEntityForDisplayOne() {
            if (itemResult != null) {
                return CoreRegistry.get(EntityManager.class).create(itemResult);
            } else {
                return CoreRegistry.get(BlockItemFactory.class).newInstance(CoreRegistry.get(BlockManager.class).getBlockFamily(blockResult));
            }
        }

        @Override
        public EntityRef getResultItemEntityForDisplayMax() {
            return null;
        }
    }
}
