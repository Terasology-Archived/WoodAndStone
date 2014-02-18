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

import org.terasology.crafting.processPart.ProcessRecipeProcessPart;
import org.terasology.crafting.processPart.ValidateRecipeProcessPart;
import org.terasology.crafting.system.recipe.workstation.CraftingStationRecipe;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.LinkedList;
import java.util.List;

public class CraftingWorkstationProcess implements WorkstationProcess {
    private String processType;
    private String craftingRecipeId;
    private CraftingStationRecipe recipe;
    private List<ProcessPart> processParts = new LinkedList<>();

    public CraftingWorkstationProcess(String processType, String craftingRecipeId, CraftingStationRecipe recipe) {
        this.processType = processType;
        this.craftingRecipeId = craftingRecipeId;
        this.recipe = recipe;

        processParts.add(new ValidateRecipeProcessPart(recipe));
        processParts.add(new ProcessRecipeProcessPart(recipe));
    }

    @Override
    public String getProcessType() {
        return processType;
    }

    @Override
    public String getId() {
        return craftingRecipeId;
    }

    @Override
    public List<ProcessPart> getProcessParts() {
        return processParts;
    }

    public CraftingStationRecipe getCraftingWorkstationRecipe() {
        return recipe;
    }
}
