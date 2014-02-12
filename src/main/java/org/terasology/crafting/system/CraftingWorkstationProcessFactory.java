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
package org.terasology.crafting.system;

import org.terasology.crafting.component.CraftingStationRecipeComponent;
import org.terasology.crafting.system.recipe.workstation.SimpleWorkstationRecipe;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationProcessFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingWorkstationProcessFactory implements WorkstationProcessFactory {
    @Override
    public WorkstationProcess createProcess(Prefab prefab) {
        CraftingStationRecipeComponent recipe = prefab.getComponent(CraftingStationRecipeComponent.class);

        SimpleWorkstationRecipe workstationRecipe = new SimpleWorkstationRecipe();
        for (String recipeComponent : recipe.recipeComponents) {
            String[] split = recipeComponent.split("\\*", 2);
            int count = Integer.parseInt(split[0]);
            String type = split[1];
            workstationRecipe.addIngredient(type, count);
        }
        for (String recipeTool : recipe.recipeTools) {
            String[] split = recipeTool.split("\\*", 2);
            int count = Integer.parseInt(split[0]);
            String type = split[1];
            workstationRecipe.addRequiredTool(type, count);
        }

        if (recipe.blockResult != null) {
            String[] split = recipe.blockResult.split("\\*", 2);
            int count = Integer.parseInt(split[0]);
            String block = split[1];
            workstationRecipe.setBlockResult(block, (byte) count);
        }
        if (recipe.itemResult != null) {
            String[] split = recipe.itemResult.split("\\*", 2);
            int count = Integer.parseInt(split[0]);
            String item = split[1];
            workstationRecipe.setItemResult(item, (byte) count);
        }
        return new CraftingWorkstationProcess(recipe.stationType, recipe.recipeId, workstationRecipe);
    }
}
