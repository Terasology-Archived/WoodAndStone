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
import org.terasology.was.system.recipe.hand.CompositeTypeBasedCraftInHandRecipe;
import org.terasology.was.system.recipe.hand.CraftInHandRecipe;
import org.terasology.was.system.recipe.hand.SimpleConsumingCraftInHandRecipe;
import org.terasology.was.system.recipe.hand.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.was.system.recipe.hand.behaviour.DoNothingCraftBehaviour;
import org.terasology.was.system.recipe.hand.behaviour.ReduceItemDurabilityCraftBehaviour;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RegisterWoodAndStoneRecipes implements ComponentSystem {
    @In
    private CraftInHandRecipeRegistry recipeRegistry;

    @Override
    public void initialise() {
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

    public void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe) {
        recipeRegistry.addCraftInHandRecipe(recipeId, craftInHandRecipe);
    }


    @Override
    public void shutdown() {
    }
}
