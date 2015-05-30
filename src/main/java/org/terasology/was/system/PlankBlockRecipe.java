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
import org.terasology.crafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.crafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.render.result.BlockRecipeResultFactory;
import org.terasology.crafting.system.recipe.workstation.AbstractWorkstationRecipe;
import org.terasology.crafting.system.recipe.workstation.CraftingStationIngredientPredicate;
import org.terasology.crafting.system.recipe.workstation.CraftingStationToolPredicate;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.registry.CoreRegistry;
import org.terasology.was.component.TreeTypeComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

import java.util.List;

public class PlankBlockRecipe extends AbstractWorkstationRecipe {
    public PlankBlockRecipe(int ingredientCount, int toolDurability, String shape, int resultCount) {
        Predicate<EntityRef> plankPredicate = new CraftingStationIngredientPredicate("WoodAndStone:plank");
        Predicate<EntityRef> hammerPredicate = new CraftingStationToolPredicate("hammer");

        addIngredientBehaviour(new ConsumePlankIngredientBehaviour(plankPredicate, ingredientCount, new InventorySlotTypeResolver("INPUT")));
        addToolBehaviour(new ReduceDurabilityCraftBehaviour(hammerPredicate, toolDurability, new InventorySlotTypeResolver("TOOL")));

        setResultFactory(new PlankBlockRecipeResultFactory(shape, resultCount));
    }

    private final class PlankBlockRecipeResultFactory extends BlockRecipeResultFactory {
        private String shape;

        private PlankBlockRecipeResultFactory(String shape, int count) {
            super(count);
            this.shape = shape;
        }

        @Override
        protected Block getBlock(List<String> parameters) {
            String[] split = parameters.get(0).split("\\|");
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            if (split.length == 2) {
                String treeType = split[1];
                String blockType = "WoodAndStone:" + treeType + "Plank";
                BlockUri customBlockUri = new BlockUri("WoodAndStone:" + treeType + "Plank");
                if (blockManager.getBlockFamily(customBlockUri) != null) {
                    return blockManager.getBlockFamily(appendShapeIfNeeded(blockType)).getArchetypeBlock();
                }
            }
            return blockManager.getBlockFamily(appendShapeIfNeeded("Core:Plank")).getArchetypeBlock();
        }

        private String appendShapeIfNeeded(String value) {
            if (shape != null) {
                return value + ":" + shape;
            } else {
                return value;
            }
        }
    }

    private final class ConsumePlankIngredientBehaviour extends ConsumeItemCraftBehaviour {
        private ConsumePlankIngredientBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
            super(matcher, count, resolver);
        }

        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            final TreeTypeComponent treeType = item.getComponent(TreeTypeComponent.class);
            if (treeType != null) {
                return super.getParameter(slots, item) + "|" + treeType.treeType;
            } else {
                return super.getParameter(slots, item);
            }
        }

        @Override
        protected List<Integer> getSlots(String parameter) {
            final String[] split = parameter.split("\\|");
            return super.getSlots(split[0]);
        }
    }
}
