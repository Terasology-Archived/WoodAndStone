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
package org.terasology.crafting.system.recipe.hand;

import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.rendering.nui.layers.ingame.inventory.ItemIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CompositeTypeBasedCraftInHandRecipe implements CraftInHandRecipe {
    private List<IngredientCraftBehaviour<EntityRef>> itemCraftBehaviours = new ArrayList<>();
    private RecipeResultFactory resultFactory;

    public CompositeTypeBasedCraftInHandRecipe(RecipeResultFactory resultFactory) {
        this.resultFactory = resultFactory;
    }

    public void addItemCraftBehaviour(IngredientCraftBehaviour<EntityRef> itemCraftBehaviour) {
        itemCraftBehaviours.add(itemCraftBehaviour);
    }

    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        // TODO: Improve searching for different kinds of items of the same type in whole inventory, not just first matching
        List<String> parameters = new LinkedList<>();
        for (IngredientCraftBehaviour<EntityRef> itemCraftBehaviour : itemCraftBehaviours) {
            String parameter = findParameter(character, itemCraftBehaviour);
            if (parameter == null) {
                return null;
            }
            parameters.add(parameter);
        }

        return Collections.<CraftInHandResult>singletonList(new CraftResult(parameters));
    }

    private String findParameter(EntityRef character, IngredientCraftBehaviour itemCraftBehaviour) {
        final List<String> validToCraft = itemCraftBehaviour.getValidToCraft(character, 1);
        if (validToCraft.size() > 0) {
            return validToCraft.get(0);
        }
        return null;
    }

    @Override
    public CraftInHandResult getResultByParameters(List<String> parameters) {
        return new CraftResult(parameters);
    }

    public class CraftResult implements CraftInHandResult {
        private List<String> parameters;
        private List<CraftIngredientRenderer> renderers;

        public CraftResult(List<String> parameters) {
            this.parameters = parameters;
        }

        @Override
        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                for (int i = 0; i < parameters.size(); i++) {
                    renderers.add(itemCraftBehaviours.get(i).getRenderer(entity, parameters.get(i)));
                }
            }
            return renderers;
        }

        @Override
        public int getMaxMultiplier(EntityRef entity) {
            int maxMultiplier = resultFactory.getMaxMultiplier(parameters);
            for (int i = 0; i < parameters.size(); i++) {
                maxMultiplier = Math.min(maxMultiplier, itemCraftBehaviours.get(i).getMaxMultiplier(entity, parameters.get(i)));
            }
            return maxMultiplier;
        }

        @Override
        public int getResultQuantity() {
            return 1;
        }

        @Override
        public long getProcessDuration() {
            return 0;
        }

        @Override
        public void setupResultDisplay(ItemIcon itemIcon) {
            resultFactory.setupDisplay(parameters, itemIcon);
        }

        private EntityRef createResult(int multiplier) {
            return resultFactory.createResult(parameters, multiplier);
        }

        @Override
        public boolean isValidForCrafting(EntityRef entity, int multiplier) {
            for (int i = 0; i < parameters.size(); i++) {
                if (!itemCraftBehaviours.get(i).isValidToCraft(entity, parameters.get(i), multiplier)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public EntityRef craft(EntityRef character, int count) {
            if (!isValidForCrafting(character, count)) {
                return EntityRef.NULL;
            }

            for (int i = 0; i < parameters.size(); i++) {
                itemCraftBehaviours.get(i).processIngredient(character, character, parameters.get(i), count);
            }

            return createResult(count);
        }
    }
}
