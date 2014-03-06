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
package org.terasology.was.ui;

import com.google.common.base.Predicate;
import org.terasology.asset.Assets;
import org.terasology.crafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.crafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.crafting.system.recipe.render.result.ItemRecipeResultFactory;
import org.terasology.crafting.system.recipe.workstation.AbstractWorkstationRecipe;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.HerbComponent;
import org.terasology.herbalism.Herbalism;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;

import java.util.List;

public class HerbalismCraftingStationRecipe extends AbstractWorkstationRecipe {
    public HerbalismCraftingStationRecipe() {
        Predicate<EntityRef> herbComponentPredicate = new Predicate<EntityRef>() {
            @Override
            public boolean apply(EntityRef input) {
                return input.hasComponent(HerbComponent.class);
            }
        };
        addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(herbComponentPredicate, 1, new InventorySlotTypeResolver("INPUT")));
        addFluidBehaviour(new ConsumeFluidBehaviour("Fluid:Water", 0.2f, new InventorySlotTypeResolver("FLUID_INPUT")));
        setRequiredHeat(95f);
        setProcessingDuration(10000);
        setResultFactory(new PotionRecipeResultFactory(Assets.getPrefab("WoodAndStone:HerbPotion"), 1));
    }

    private class PotionRecipeResultFactory extends ItemRecipeResultFactory {
        private PotionRecipeResultFactory(Prefab prefab, int count) {
            super(prefab, count);
        }

        @Override
        public void setupDisplay(List<String> parameters, ItemIcon itemIcon) {
            super.setupDisplay(parameters, itemIcon);
            final String herbParameter = parameters.get(0);
            final String herbName = herbParameter.split("\\|")[3];
            itemIcon.setTooltip("Extract from "+herbName);
        }

        @Override
        public EntityRef createResult(List<String> parameters, int multiplier) {
            final EntityRef result = super.createResult(parameters, multiplier);
            final String herbParameter = parameters.get(0);
            final String[] herbSplit = herbParameter.split("\\|");
            final String genomeId = herbSplit[1];
            final String genes = herbSplit[2];
            GenomeComponent genome = new GenomeComponent();
            genome.genomeId = genomeId;
            genome.genes = genes;
            result.addComponent(genome);
            return result;
        }
    }

    private class ConsumeHerbIngredientBehaviour extends ConsumeItemCraftBehaviour {
        private ConsumeHerbIngredientBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
            super(matcher, count, resolver);
        }

        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            final GenomeComponent genome = item.getComponent(GenomeComponent.class);
            final String herbName = CoreRegistry.get(GenomeManager.class).getGenomeProperty(item, Herbalism.NAME_PROPERTY, String.class);
            return super.getParameter(slots, item) + "|" + genome.genomeId + "|" + genome.genes + "|" + herbName;
        }

        @Override
        protected List<Integer> getSlots(String parameter) {
            return super.getSlots(parameter.substring(0, parameter.indexOf('|')));
        }
    }
}
