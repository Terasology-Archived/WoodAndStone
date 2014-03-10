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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.farm.component.SeedComponent;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.world.block.Block;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SeedingHerbRecipe implements CraftInHandRecipe {
    private static final IngredientCraftBehaviour<EntityRef> KNIFE_BEHAVIOUR = new ReduceDurabilityCraftBehaviour(
            new CraftInHandIngredientPredicate("WoodAndStone:knife"), 1, PlayerInventorySlotResolver.singleton());
    private static final ConsumeHerbBehaviour HERB_BEHAVIOUR = new ConsumeHerbBehaviour();

    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        String knifeParameter = getKnifeParameter(character);
        if (knifeParameter == null) {
            return null;
        }

        List<CraftInHandResult> results = new LinkedList<>();

        final List<String> herbParameters = HERB_BEHAVIOUR.getValidToCraft(character, 1);
        for (String herbParameter : herbParameters) {
            results.add(new Result(Arrays.asList(knifeParameter, herbParameter)));
        }

        return results;
    }

    private String getKnifeParameter(EntityRef character) {
        List<String> parameters = KNIFE_BEHAVIOUR.getValidToCraft(character, 1);
        if (parameters.size() > 0) {
            return parameters.get(0);
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
            maxMultiplier = Math.min(maxMultiplier, HERB_BEHAVIOUR.getMaxMultiplier(entity, parameters.get(1)));
            return maxMultiplier;
        }

        @Override
        public EntityRef craft(EntityRef character, int count) {
            if (!isValidForCrafting(character, count)) {
                return EntityRef.NULL;
            }

            KNIFE_BEHAVIOUR.processIngredient(character, character, parameters.get(0), count);
            HERB_BEHAVIOUR.processIngredient(character, character, parameters.get(1), count);

            final EntityRef herbSeed = CoreRegistry.get(EntityManager.class).create("WoodAndStone:HerbSeedBase");
            final GenomeManager genomeManager = CoreRegistry.get(GenomeManager.class);

            GenomeComponent genomeComponent = new GenomeComponent();
            genomeComponent.genomeId = "Herbalism:Herb";
            genomeComponent.genes = HERB_BEHAVIOUR.getSeedGenome(parameters.get(1));
            herbSeed.addComponent(genomeComponent);

            DisplayNameComponent displayName = new DisplayNameComponent();
            displayName.name = "Seeds of " + HERB_BEHAVIOUR.getHerbName(parameters.get(1));
            herbSeed.saveComponent(displayName);

            SeedComponent seedComponent = new SeedComponent();
            seedComponent.blockPlaced = genomeManager.getGenomeProperty(herbSeed, Herbalism.PLANTED_BLOCK, Block.class);
            herbSeed.addComponent(seedComponent);

            return herbSeed;
        }

        @Override
        public boolean isValidForCrafting(EntityRef entity, int multiplier) {
            if (!KNIFE_BEHAVIOUR.isValidToCraft(entity, parameters.get(0), multiplier)) {
                return false;
            }
            if (!HERB_BEHAVIOUR.isValidToCraft(entity, parameters.get(1), multiplier)) {
                return false;
            }
            return true;
        }

        @Override
        public List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                renderers.add(HERB_BEHAVIOUR.getRenderer(entity, parameters.get(1)));
                renderers.add(KNIFE_BEHAVIOUR.getRenderer(entity, parameters.get(0)));
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
            Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab("WoodAndStone:HerbSeedBase");

            itemIcon.setIcon(prefab.getComponent(ItemComponent.class).icon);
            itemIcon.setTooltip("Seeds of " + HERB_BEHAVIOUR.getHerbName(parameters.get(1)));
        }
    }

    private static class ConsumeHerbBehaviour extends ConsumeItemCraftBehaviour {
        public ConsumeHerbBehaviour() {
            super(new Predicate<EntityRef>() {
                @Override
                public boolean apply(EntityRef input) {
                    return input.hasComponent(HerbComponent.class) && input.hasComponent(GenomeComponent.class);
                }
            }, 1, PlayerInventorySlotResolver.singleton());
        }

        @Override
        protected List<Integer> getSlots(String parameter) {
            return super.getSlots(parameter.substring(0, parameter.indexOf('|')));
        }

        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            final GenomeComponent genome = item.getComponent(GenomeComponent.class);

            final GenomeManager genomeManager = CoreRegistry.get(GenomeManager.class);
            String herbName = genomeManager.getGenomeProperty(item, Herbalism.NAME_PROPERTY, String.class);

            return super.getParameter(slots, item) + "|" + genome.genes + "|" + herbName;
        }

        public String getSeedGenome(String parameter) {
            return parameter.split("\\|")[1];
        }

        public String getHerbName(String parameter) {
            return parameter.split("\\|")[2];
        }
    }
}
