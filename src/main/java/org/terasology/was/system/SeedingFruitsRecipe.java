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

import com.google.common.base.Predicate;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.CraftInHandIngredientPredicate;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.PlayerInventorySlotResolver;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.durability.ReduceDurabilityEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SeedingFruitsRecipe implements CraftInHandRecipe {
    private static final IngredientCraftBehaviour<EntityRef, Integer> KNIFE_BEHAVIOUR = new ReduceDurabilityCraftBehaviour(
            new CraftInHandIngredientPredicate("WoodAndStone:knife"), 1, PlayerInventorySlotResolver.singleton());
    private static final IngredientCraftBehaviour<EntityRef, Integer> FRUIT_BEHAVIOUR = new ConsumeItemCraftBehaviour(
            new Predicate<EntityRef>() {
                @Override
                public boolean apply(EntityRef input) {
                    Prefab prefab = input.getParentPrefab();
                    return prefab != null && prefab.getURI().getNormalisedModuleName().equals("plantpack")
                            && prefab.getURI().getNormalisedAssetName().endsWith("fruit");
                }
            }, 1, PlayerInventorySlotResolver.singleton());

    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        int knifeSlot = getKnifeSlot(character);
        if (knifeSlot == -1) {
            return null;
        }

        int maxKnifeMultiplier = KNIFE_BEHAVIOUR.getMaxMultiplier(character, knifeSlot);

        List<CraftInHandResult> results = new LinkedList<>();
        Set<String> usedFruits = new HashSet<>();

        for (int slot : FRUIT_BEHAVIOUR.getValidToCraft(character, 1)) {
            EntityRef fruitItem = InventoryUtils.getItemAt(character, slot);
            Prefab prefab = fruitItem.getParentPrefab();
            if (!usedFruits.contains(prefab.getURI().getNormalisedAssetName())) {
                String assetName = prefab.getURI().getNormalisedAssetName();
                String fruitName = assetName.substring(0, assetName.length() - 5);
                usedFruits.add(fruitName);
                ItemComponent item = fruitItem.getComponent(ItemComponent.class);
                results.add(new Result(character, slot, knifeSlot, Math.min(maxKnifeMultiplier, item.stackCount)));
            }
        }

        return results;
    }

    private int getKnifeSlot(EntityRef character) {
        List<Integer> slots = KNIFE_BEHAVIOUR.getValidToCraft(character, 1);
        if (slots.size() > 0) {
            return slots.get(0);
        }
        return -1;
    }

    @Override
    public CraftInHandResult getResultById(EntityRef character, String resultId) {
        String[] split = resultId.split("\\|");

        return new Result(character, Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    public static final class Result implements CraftInHandResult {
        private EntityRef character;
        private int fruitSlot;
        private int knifeSlot;
        private int maxMultiplier;
        private List<CraftIngredientRenderer> renderers;

        private Result(EntityRef character, int fruitSlot, int knifeSlot, int maxMultiplier) {
            this.character = character;
            this.fruitSlot = fruitSlot;
            this.knifeSlot = knifeSlot;
            this.maxMultiplier = maxMultiplier;
        }

        @Override
        public String getResultId() {
            return fruitSlot + "|" + knifeSlot + "|" + maxMultiplier;
        }

        @Override
        public int getMaxMultiplier() {
            return maxMultiplier;
        }

        @Override
        public EntityRef craft(EntityRef character, int count) {
            EntityRef fruit = InventoryUtils.getItemAt(character, fruitSlot);

            String assetName = fruit.getParentPrefab().getURI().getNormalisedAssetName();
            String fruitName = assetName.substring(0, assetName.length() - 5);

            character.send(new RemoveItemAction(character, fruit, true, count));

            EntityRef knife = InventoryUtils.getItemAt(character, knifeSlot);
            knife.send(new ReduceDurabilityEvent(count));

            BlockFamily blockFamily = CoreRegistry.get(BlockManager.class).getBlockFamily("PlantPack:" + fruitName + "1");
            return new BlockItemFactory(CoreRegistry.get(EntityManager.class)).newInstance(blockFamily, 1);
        }

        @Override
        public List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                renderers.add(FRUIT_BEHAVIOUR.getRenderer(entity, fruitSlot));
                renderers.add(KNIFE_BEHAVIOUR.getRenderer(entity, knifeSlot));
            }
            return renderers;
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
