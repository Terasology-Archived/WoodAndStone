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
package org.terasology.was.system;

import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.was.system.hand.CraftInHandRecipeRegistry;
import org.terasology.was.system.hand.recipe.CompositeTypeBasedCraftInHandRecipe;
import org.terasology.was.system.hand.recipe.CraftInHandRecipe;
import org.terasology.was.system.hand.recipe.SimpleConsumingCraftInHandRecipe;
import org.terasology.was.system.hand.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.was.system.hand.recipe.behaviour.DoNothingCraftBehaviour;
import org.terasology.was.system.hand.recipe.behaviour.ReduceItemDurabilityCraftBehaviour;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;
import org.terasology.workstation.system.recipe.CraftingStationRecipe;
import org.terasology.workstation.system.recipe.SimpleWorkstationRecipe;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RegisterWoodAndStoneRecipes implements ComponentSystem {
    @In
    private CraftInHandRecipeRegistry recipeRegistry;
    @In
    private CraftingStationRecipeRegistry stationRecipeRegistry;

    @Override
    public void initialise() {
        addCraftInHandRecipes();

        addWorkstationRecipes();
    }

    private void addCraftInHandRecipes() {
        addCraftInHandRecipe("WoodAndStone:CrudeHammer",
                new SimpleConsumingCraftInHandRecipe("stick", "binding", "stone", "WoodAndStone:CrudeHammer"));
        addCraftInHandRecipe("WoodAndStone:CrudeAxe",
                new SimpleConsumingCraftInHandRecipe("stick", "binding", "sharpStone", "WoodAndStone:CrudeAxe"));

        addCraftInHandRecipe("WoodAndStone:sharpStone",
                new CompositeTypeBasedCraftInHandRecipe(
                        "stone", new ConsumeItemCraftBehaviour("stone"),
                        "hammer", new ReduceItemDurabilityCraftBehaviour("hammer", 1),
                        null, new DoNothingCraftBehaviour(),
                        "WoodAndStone:sharpStone"));
    }

    private void addWorkstationRecipes() {
        SimpleWorkstationRecipe plankRecipe = new SimpleWorkstationRecipe();
        plankRecipe.addIngredient("WoodAndStone:wood", 1);
        plankRecipe.addRequiredTool("wood", 1);
        plankRecipe.setItemResult("WoodAndStone:stone", (byte) 1);

        addBasicWoodworkingRecipe("WoodAndStone:Plank", plankRecipe);
    }

    public void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe) {
        recipeRegistry.addCraftInHandRecipe(recipeId, craftInHandRecipe);
    }

    public void addBasicWoodworkingRecipe(String recipeId, CraftingStationRecipe recipe) {
        stationRecipeRegistry.addCraftingStationRecipe("WoodAndStone:basicWoodcrafting", recipeId, recipe);
    }


    @Override
    public void shutdown() {
    }
}
