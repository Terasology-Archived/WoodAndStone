// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.workstation;

import org.terasology.crafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.crafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.crafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class DefaultWorkstationRecipe extends AbstractWorkstationRecipe {
    public void addIngredient(String type, int count) {
        final ConsumeItemCraftBehaviour behaviour =
                new ConsumeItemCraftBehaviour(new CraftingStationIngredientPredicate(type), count,
                        new InventorySlotTypeResolver("INPUT"));
        addIngredientBehaviour(behaviour);
    }

    public void addRequiredTool(String toolType, int durability) {
        final ReduceDurabilityCraftBehaviour behaviour = new ReduceDurabilityCraftBehaviour(
                new CraftingStationToolPredicate(toolType), durability, new InventorySlotTypeResolver("TOOL"));
        addToolBehaviour(behaviour);
    }

    public void addFluid(String fluidType, float volume) {
        final ConsumeFluidBehaviour behaviour = new ConsumeFluidBehaviour(fluidType, volume,
                new InventorySlotTypeResolver("FLUID_INPUT"));
        addFluidBehaviour(behaviour);
    }
}
