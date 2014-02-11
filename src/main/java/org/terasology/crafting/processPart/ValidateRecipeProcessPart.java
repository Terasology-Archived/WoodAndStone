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
package org.terasology.crafting.processPart;

import org.terasology.crafting.system.recipe.workstation.CraftingStationRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.workstation.component.CraftingStationComponent;
import org.terasology.workstation.process.ProcessPart;

import java.util.List;

public class ValidateRecipeProcessPart implements ProcessPart {
    private CraftingStationRecipe craftingStationRecipe;

    public ValidateRecipeProcessPart(CraftingStationRecipe craftingStationRecipe) {
        this.craftingStationRecipe = craftingStationRecipe;
    }

    @Override
    public boolean validate(EntityRef instigator, EntityRef workstation) {
        CraftingStationComponent craftingStation = workstation.getComponent(CraftingStationComponent.class);
        if (craftingStation == null) {
            return false;
        }
        List<CraftingStationRecipe.CraftingStationResult> result = craftingStationRecipe.getMatchingRecipeResults(workstation,
                craftingStation.upgradeSlots + craftingStation.toolSlots, craftingStation.ingredientSlots,
                craftingStation.upgradeSlots, craftingStation.toolSlots);

        return result != null;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation) {
    }
}
