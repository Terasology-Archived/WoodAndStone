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

import org.terasology.crafting.processPart.ProcessUpgradeProcessPart;
import org.terasology.crafting.processPart.ValidateUpgradeProcessPart;
import org.terasology.crafting.processPart.WorkstationTypeProcessPart;
import org.terasology.crafting.system.recipe.workstation.UpgradeRecipe;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.LinkedList;
import java.util.List;

public class CraftingWorkstationUpgradeProcess implements WorkstationProcess {
    private String workstationType;
    private String targetWorkstationType;
    private UpgradeRecipe upgradeRecipe;
    private List<ProcessPart> processParts = new LinkedList<>();

    public CraftingWorkstationUpgradeProcess(String workstationType, String targetWorkstationType,
                                             UpgradeRecipe upgradeRecipe) {
        this.workstationType = workstationType;
        this.targetWorkstationType = targetWorkstationType;
        this.upgradeRecipe = upgradeRecipe;

        processParts.add(new WorkstationTypeProcessPart(workstationType));
        processParts.add(new ValidateUpgradeProcessPart(upgradeRecipe));
        processParts.add(new ProcessUpgradeProcessPart(upgradeRecipe));
    }

    @Override
    public String getId() {
        return "Upgrade:" + workstationType + "-" + targetWorkstationType;
    }

    public UpgradeRecipe getUpgradeRecipe() {
        return upgradeRecipe;
    }

    @Override
    public List<ProcessPart> getProcessParts() {
        return processParts;
    }
}
