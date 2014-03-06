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
package org.terasology.crafting.system.recipe.workstation;

import org.terasology.crafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.RecipeResultFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.heat.HeatUtils;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.world.BlockEntityRegistry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class DefaultWorkstationRecipe extends AbstractWorkstationRecipe {
    public void addIngredient(String type, int count) {
        final ConsumeItemCraftBehaviour behaviour = new ConsumeItemCraftBehaviour(new CraftingStationIngredientPredicate(type), count, new InventorySlotTypeResolver("INPUT"));
        addIngredientBehaviour(behaviour);
    }

    public void addRequiredTool(String toolType, int durability) {
        final ReduceDurabilityCraftBehaviour behaviour = new ReduceDurabilityCraftBehaviour(new CraftingStationToolPredicate(toolType), durability, new InventorySlotTypeResolver("TOOL"));
        addToolBehaviour(behaviour);
    }

    public void addFluid(String fluidType, float volume) {
        final ConsumeFluidBehaviour behaviour = new ConsumeFluidBehaviour(fluidType, volume, new InventorySlotTypeResolver("FLUID_INPUT"));
        addFluidBehaviour(behaviour);
    }
}
