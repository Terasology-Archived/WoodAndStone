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
import org.terasology.asset.Assets;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.CraftInHandIngredientPredicate;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.PlayerInventorySlotResolver;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SeedingFruitsRecipe implements CraftInHandRecipe {
    private static final IngredientCraftBehaviour<EntityRef> KNIFE_BEHAVIOUR = new ReduceDurabilityCraftBehaviour(
            new CraftInHandIngredientPredicate("WoodAndStone:knife"), 1, PlayerInventorySlotResolver.singleton());
    private static final ConsumeFruitBehaviour FRUIT_BEHAVIOUR = new ConsumeFruitBehaviour();

    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        String knifeSlot = getKnifeSlot(character);
        if (knifeSlot == null) {
            return null;
        }

        List<CraftInHandResult> results = new LinkedList<>();

        final List<String> fruitParameters = FRUIT_BEHAVIOUR.getValidToCraft(character, 1);
        for (String fruitParameter : fruitParameters) {
            results.add(new Result(Arrays.asList(knifeSlot, fruitParameter)));
        }

        return results;
    }

    private String getKnifeSlot(EntityRef character) {
        List<String> slots = KNIFE_BEHAVIOUR.getValidToCraft(character, 1);
        if (slots.size() > 0) {
            return slots.get(0);
        }
        return null;
    }

    @Override
    public CraftInHandResult getResultByParameters(List<String> parameters) {
        return new Result(parameters);
    }

    public static final class Result implements CraftInHandResult {
        private List<String> parameters;
        private List<CraftIngredientRenderer> renderers;

        private Result(List<String> parameters) {
            this.parameters = parameters;
        }

        @Override
        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public int getMaxMultiplier(EntityRef entity) {
            int maxMultiplier = KNIFE_BEHAVIOUR.getMaxMultiplier(entity, parameters.get(0));
            maxMultiplier = Math.min(maxMultiplier, FRUIT_BEHAVIOUR.getMaxMultiplier(entity, parameters.get(1)));
            return maxMultiplier;
        }

        @Override
        public EntityRef craft(EntityRef character, int count) {
            if (!isValidForCrafting(character, count)) {
                return EntityRef.NULL;
            }

            KNIFE_BEHAVIOUR.processIngredient(character, character, parameters.get(0), count);
            FRUIT_BEHAVIOUR.processIngredient(character, character, parameters.get(1), count);

            BlockFamily blockFamily = CoreRegistry.get(BlockManager.class).getBlockFamily(FRUIT_BEHAVIOUR.getSaplingResult(parameters.get(1)));
            return new BlockItemFactory(CoreRegistry.get(EntityManager.class)).newInstance(blockFamily, 1);
        }

        @Override
        public boolean isValidForCrafting(EntityRef entity, int multiplier) {
            if (!FRUIT_BEHAVIOUR.isValidToCraft(entity, parameters.get(0), multiplier)) {
                return false;
            }
            if (!KNIFE_BEHAVIOUR.isValidToCraft(entity, parameters.get(1), multiplier)) {
                return false;
            }
            return true;
        }

        @Override
        public List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                renderers.add(FRUIT_BEHAVIOUR.getRenderer(entity, parameters.get(0)));
                renderers.add(KNIFE_BEHAVIOUR.getRenderer(entity, parameters.get(1)));
            }
            return renderers;
        }

        @Override
        public long getProcessDuration() {
            return 0;
        }

        @Override
        public int getResultQuantity() {
            return 1;
        }

        @Override
        public void setupResultDisplay(ItemIcon itemIcon) {
            Block block = CoreRegistry.get(BlockManager.class).getBlockFamily(FRUIT_BEHAVIOUR.getSaplingResult(parameters.get(0))).getArchetypeBlock();

            itemIcon.setMesh(block.getMesh());
            itemIcon.setMeshTexture(Assets.getTexture("engine:terrain"));
            itemIcon.setTooltip(block.getDisplayName());
        }
    }

    private static class ConsumeFruitBehaviour extends ConsumeItemCraftBehaviour {
        public ConsumeFruitBehaviour() {
            super(new Predicate<EntityRef>() {
                @Override
                public boolean apply(EntityRef input) {
                    Prefab prefab = input.getParentPrefab();
                    return prefab != null && prefab.getURI().getNormalisedModuleName().equals("plantpack")
                            && prefab.getURI().getNormalisedAssetName().endsWith("fruit");
                }
            }, 1, PlayerInventorySlotResolver.singleton());
        }

        @Override
        public List<String> getValidToCraft(EntityRef entity, int multiplier) {
            List<String> baseParameters = super.getValidToCraft(entity, multiplier);
            List<String> result = new LinkedList<>();
            Set<String> usedFruits = new HashSet<>();
            for (String baseParameter : baseParameters) {
                int slot = Integer.parseInt(baseParameter);

                EntityRef fruitItem = InventoryUtils.getItemAt(entity, slot);
                Prefab prefab = fruitItem.getParentPrefab();
                if (!usedFruits.contains(prefab.getURI().getNormalisedAssetName())) {
                    String assetName = prefab.getURI().getNormalisedAssetName();
                    String fruitName = assetName.substring(0, assetName.length() - 5);
                    usedFruits.add(fruitName);

                    result.add(slot + "|" + fruitName);
                }
            }

            return result;
        }

        private String getBaseParameter(String parameter) {
            return parameter.substring(0, parameter.indexOf('|'));
        }

        @Override
        public int getMaxMultiplier(EntityRef entity, String parameter) {
            return super.getMaxMultiplier(entity, getBaseParameter(parameter));
        }

        @Override
        public CraftIngredientRenderer getRenderer(EntityRef entity, String parameter) {
            return super.getRenderer(entity, getBaseParameter(parameter));
        }

        @Override
        public boolean isValidToCraft(EntityRef entity, String parameter, int multiplier) {
            return super.isValidToCraft(entity, getBaseParameter(parameter), multiplier);
        }

        @Override
        public void processIngredient(EntityRef instigator, EntityRef entity, String parameter, int multiplier) {
            super.processIngredient(instigator, entity, getBaseParameter(parameter), multiplier);
        }

        public String getSaplingResult(String parameter) {
            return "PlantPack:" + parameter.substring(parameter.indexOf('|') + 1) + "1";
        }
    }
}
