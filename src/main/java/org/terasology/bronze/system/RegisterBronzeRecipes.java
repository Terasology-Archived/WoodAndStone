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
package org.terasology.bronze.system;

import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.UniformMultiBlockFormItemRecipe;
import org.terasology.registry.In;
import org.terasology.was.system.ToolTypeEntityFilter;
import org.terasology.was.system.UseOnTopFilter;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;
import org.terasology.workstation.system.recipe.SimpleWorkstationRecipe;
import org.terasology.world.block.BlockUri;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RegisterBronzeRecipes implements ComponentSystem {
    @In
    private CraftingStationRecipeRegistry craftingStationRecipeRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockRecipeRegistry;

    @Override
    public void initialise() {
        addWorkstationRecipes();

        addBasicMetalcraftingRecipes();
    }

    private void addBasicMetalcraftingRecipes() {
        SimpleWorkstationRecipe chalcopyriteDustRecipe = new SimpleWorkstationRecipe();
        chalcopyriteDustRecipe.addIngredient("WoodAndStone:copperOre", 1);
        chalcopyriteDustRecipe.addRequiredTool("stone", 1);
        chalcopyriteDustRecipe.setItemResult("WoodAndStone:CopperOreDust", (byte) 1);

        craftingStationRecipeRegistry.addCraftingStationRecipe(
                "WoodAndStone:BasicMetalcrafting", "WoodAndStone:ChalcopyriteDust", chalcopyriteDustRecipe);
    }

    private void addWorkstationRecipes() {
        multiBlockRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(
                        new ToolTypeEntityFilter("stone"), new UseOnTopFilter(),
                        new BlockUriEntityFilter(new BlockUri("Core", "CobbleStone")), new Vector3i(2, 1, 1),
                        "WoodAndStone:BasicMetalcrafting", "WoodAndStone:BasicMetalStation"));
    }

    @Override
    public void shutdown() {
    }
}
