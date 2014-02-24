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
import org.terasology.crafting.system.recipe.render.result.BlockRecipeResultFactory;
import org.terasology.crafting.system.recipe.render.result.ItemRecipeResultFactory;
import org.terasology.crafting.system.recipe.workstation.SimpleWorkstationRecipe;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.component.ProcessDefinitionComponent;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationProcessFactory;
import org.terasology.world.block.BlockManager;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingWorkstationProcessFactory implements WorkstationProcessFactory {
    @Override
    public WorkstationProcess createProcess(Prefab prefab) {
        ProcessDefinitionComponent process = prefab.getComponent(ProcessDefinitionComponent.class);

        CraftingStationRecipeComponent recipe = prefab.getComponent(CraftingStationRecipeComponent.class);

        SimpleWorkstationRecipe workstationRecipe = new SimpleWorkstationRecipe();
        if (recipe.recipeComponents != null) {
            for (String recipeComponent : recipe.recipeComponents) {
                String[] split = recipeComponent.split("\\*", 2);
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                workstationRecipe.addIngredient(type, count);
            }
        }
        if (recipe.recipeTools != null) {
            for (String recipeTool : recipe.recipeTools) {
                String[] split = recipeTool.split("\\*", 2);
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                workstationRecipe.addRequiredTool(type, count);
            }
        }
        if (recipe.recipeFluids != null) {
            for (String recipeFluid : recipe.recipeFluids) {
                String[] split = recipeFluid.split("\\*", 2);
                float volume = Float.parseFloat(split[0]);
                String type = split[1];
                workstationRecipe.addFluid(type, volume);
            }
        }

        if (recipe.blockResult != null) {
            final BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            String[] split = recipe.blockResult.split("\\*", 2);
            int count = Integer.parseInt(split[0]);
            String block = split[1];
            workstationRecipe.setResultFactory(new BlockRecipeResultFactory(blockManager.getBlockFamily(block).getArchetypeBlock(), count));
        }
        if (recipe.itemResult != null) {
            final PrefabManager prefabManager = CoreRegistry.get(PrefabManager.class);
            String[] split = recipe.itemResult.split("\\*", 2);
            int count = Integer.parseInt(split[0]);
            String item = split[1];
            workstationRecipe.setResultFactory(new ItemRecipeResultFactory(prefabManager.getPrefab(item), count));
        }

        if (recipe.processingDuration != 0) {
            workstationRecipe.setProcessingDuration(recipe.processingDuration);
        }
        if (recipe.requiredTemperature > 0) {
            workstationRecipe.setRequiredHeat(recipe.requiredTemperature);
        }

        return new CraftingWorkstationProcess(process.processType, recipe.recipeId, workstationRecipe);
    }
}
