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
package org.terasology.crafting.system.recipe;

import org.terasology.crafting.component.CraftInHandRecipeComponent;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CompositeTypeBasedCraftInHandRecipe implements CraftInHandRecipe {
    private String item1Type;
    private ItemCraftBehaviour itemCraftBehaviour1;
    private String item2Type;
    private ItemCraftBehaviour itemCraftBehaviour2;
    private String item3Type;
    private ItemCraftBehaviour itemCraftBehaviour3;
    private String prefabName;
    private boolean block;

    public CompositeTypeBasedCraftInHandRecipe(String item1Type, ItemCraftBehaviour itemCraftBehaviour1,
                                               String item2Type, ItemCraftBehaviour itemCraftBehaviour2,
                                               String item3Type, ItemCraftBehaviour itemCraftBehaviour3,
                                               String resultPrefab) {
        this(item1Type, itemCraftBehaviour1, item2Type, itemCraftBehaviour2, item3Type, itemCraftBehaviour3, resultPrefab, false);
    }

    public CompositeTypeBasedCraftInHandRecipe(String item1Type, ItemCraftBehaviour itemCraftBehaviour1,
                                               String item2Type, ItemCraftBehaviour itemCraftBehaviour2,
                                               String item3Type, ItemCraftBehaviour itemCraftBehaviour3,
                                               String resultPrefab, boolean block) {
        this.item1Type = item1Type;
        this.itemCraftBehaviour1 = itemCraftBehaviour1;
        this.item2Type = item2Type;
        this.itemCraftBehaviour2 = itemCraftBehaviour2;
        this.item3Type = item3Type;
        this.itemCraftBehaviour3 = itemCraftBehaviour3;
        this.prefabName = resultPrefab;
        this.block = block;
    }

    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        // TODO: Improve searching for different kinds of items of the same type in whole inventory, not just first matching
        int slot1 = item1Type != null ? hasItem(character, item1Type) : -1;
        int slot2 = item2Type != null ? hasItem(character, item2Type) : -1;
        int slot3 = item3Type != null ? hasItem(character, item3Type) : -1;
        if ((slot1 != -1 || item1Type == null) && (slot2 != -1 || item2Type == null) && (slot3 != -1 || item3Type == null)) {
            return Collections.<CraftInHandResult>singletonList(new CraftResult(slot1, slot2, slot3));
        }
        return null;
    }

    @Override
    public CraftInHandResult getResultById(String resultId) {
        String[] slots = resultId.split("\\|");
        return new CraftResult(Integer.parseInt(slots[0]), Integer.parseInt(slots[1]), Integer.parseInt(slots[2]));
    }

    private int hasItem(EntityRef character, String itemType) {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        int numSlots = inventoryManager.getNumSlots(character);
        for (int i = 0; i < numSlots; i++) {
            if (hasItemInSlot(character, itemType, inventoryManager, i)) {
                return i;
            }
        }

        return -1;
    }

    private boolean hasItemInSlot(EntityRef character, String itemType, SlotBasedInventoryManager inventoryManager, int slot) {
        CraftInHandRecipeComponent component = inventoryManager.getItemInSlot(character, slot).getComponent(CraftInHandRecipeComponent.class);
        if (component != null && component.componentType.equals(itemType)) {
            return true;
        }
        return false;
    }

    public class CraftResult implements CraftInHandResult {
        private int slot1;
        private int slot2;
        private int slot3;

        public CraftResult(int slot1, int slot2, int slot3) {
            this.slot1 = slot1;
            this.slot2 = slot2;
            this.slot3 = slot3;
        }

        @Override
        public String getResultId() {
            return slot1 + "|" + slot2 + "|" + slot3;
        }

        @Override
        public Map<Integer, Integer> getComponentSlotAndCount() {
            Map<Integer, Integer> result = new LinkedHashMap<>();
            if (slot1 != -1) {
                result.put(slot1, 1);
            }
            if (slot2 != -1) {
                result.put(slot2, 1);
            }
            if (slot3 != -1) {
                result.put(slot3, 1);
            }
            return result;
        }

        @Override
        public EntityRef createResultItemEntityForDisplayOne() {
            if (block) {
                BlockFamily blockFamily = CoreRegistry.get(BlockManager.class).getBlockFamily(prefabName);
                return new BlockItemFactory(CoreRegistry.get(EntityManager.class)).newInstance(blockFamily, 1);
            } else {
                return CoreRegistry.get(EntityManager.class).create(prefabName);
            }
        }

        @Override
        public EntityRef getResultItemEntityForDisplayMax() {
            return null;
        }

        @Override
        public EntityRef craftOne(EntityRef character) {
            SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
            EntityRef item1 = inventoryManager.getItemInSlot(character, slot1);
            EntityRef item2 = inventoryManager.getItemInSlot(character, slot2);
            EntityRef item3 = inventoryManager.getItemInSlot(character, slot3);
            if (itemCraftBehaviour1.isValid(character, item1)
                    && itemCraftBehaviour2.isValid(character, item2)
                    && itemCraftBehaviour3.isValid(character, item3)) {
                itemCraftBehaviour1.processForItem(character, item1);
                itemCraftBehaviour2.processForItem(character, item2);
                itemCraftBehaviour3.processForItem(character, item3);

                return createResultItemEntityForDisplayOne();
            }

            return EntityRef.NULL;
        }
    }
}
