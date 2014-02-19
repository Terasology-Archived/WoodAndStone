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
package org.terasology.was.system;

import org.terasology.crafting.component.CraftInHandIngredientComponent;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.durability.ReduceDurabilityEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SeedingFruitsRecipe implements CraftInHandRecipe {
    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        int slotCount = InventoryUtils.getSlotCount(character);
        int knifeSlot = getKnifeSlot(character, slotCount);
        if (knifeSlot == -1) {
            return null;
        }

        List<CraftInHandResult> results = new LinkedList<>();
        Set<String> usedFruits = new HashSet<>();

        for (int i = 0; i < slotCount; i++) {
            Prefab prefab = InventoryUtils.getItemAt(character, i).getParentPrefab();
            if (prefab != null && prefab.getURI().getNormalisedModuleName().equals("plantpack")
                    && prefab.getURI().getNormalisedAssetName().endsWith("fruit")
                    && !usedFruits.contains(prefab.getURI().getNormalisedAssetName())) {
                String assetName = prefab.getURI().getNormalisedAssetName();
                String fruitName = assetName.substring(0, assetName.length() - 5);
                usedFruits.add(fruitName);
                results.add(new Result(character, i, knifeSlot));
            }
        }

        return results;
    }

    private int getKnifeSlot(EntityRef character, int slotCount) {
        for (int i = 0; i < slotCount; i++) {
            CraftInHandIngredientComponent recipeComponent = InventoryUtils.getItemAt(character, i).getComponent(CraftInHandIngredientComponent.class);
            if (recipeComponent != null && recipeComponent.componentType.equals("WoodAndStone:knife")) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public CraftInHandResult getResultById(EntityRef character, String resultId) {
        String[] split = resultId.split("\\|");

        return new Result(character, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public static final class Result implements CraftInHandResult {
        private EntityRef character;
        private int fruitSlot;
        private int knifeSlot;

        private Result(EntityRef character, int fruitSlot, int knifeSlot) {
            this.character = character;
            this.fruitSlot = fruitSlot;
            this.knifeSlot = knifeSlot;
        }

        @Override
        public String getResultId() {
            return fruitSlot + "|" + knifeSlot;
        }

        @Override
        public EntityRef craftOne(EntityRef character) {
            EntityRef fruit = InventoryUtils.getItemAt(character, fruitSlot);

            String assetName = fruit.getParentPrefab().getURI().getNormalisedAssetName();
            String fruitName = assetName.substring(0, assetName.length() - 5);

            character.send(new RemoveItemAction(character, fruit, true, 1));

            EntityRef knife = InventoryUtils.getItemAt(character, knifeSlot);
            knife.send(new ReduceDurabilityEvent(1));

            BlockFamily blockFamily = CoreRegistry.get(BlockManager.class).getBlockFamily("PlantPack:" + fruitName + "1");
            return new BlockItemFactory(CoreRegistry.get(EntityManager.class)).newInstance(blockFamily, 1);
        }

        @Override
        public Map<Integer, Integer> getComponentSlotAndCount() {
            Map<Integer, Integer> result = new LinkedHashMap<>();
            result.put(fruitSlot, 1);
            result.put(knifeSlot, 1);
            return result;
        }

        @Override
        public int getResultQuantity() {
            return 1;
        }

        @Override
        public Block getResultBlock() {
            String assetName = InventoryUtils.getItemAt(character, fruitSlot).getParentPrefab().getURI().getNormalisedAssetName();
            String fruitName = assetName.substring(0, assetName.length() - 5);
            return CoreRegistry.get(BlockManager.class).getBlockFamily("PlantPack:" + fruitName + "1").getArchetypeBlock();
        }

        @Override
        public Prefab getResultItem() {
            return null;
        }
    }
}
