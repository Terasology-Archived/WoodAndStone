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

import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.crafting.system.recipe.workstation.CraftingStationRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.workstation.process.ProcessPart;

import java.util.Set;

public class ProcessRecipeProcessPart implements ProcessPart {
    private CraftingStationRecipe craftingStationRecipe;

    public ProcessRecipeProcessPart(CraftingStationRecipe craftingStationRecipe) {
        this.craftingStationRecipe = craftingStationRecipe;
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation) {
        return null;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String resultId) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String resultId) {
        CraftingStationComponent craftingStation = workstation.getComponent(CraftingStationComponent.class);
        CraftingStationRecipe.CraftingStationResult craftingStationResult = craftingStationRecipe.getResultById(resultId);
        int resultSlot = craftingStation.upgradeSlots + craftingStation.toolSlots + craftingStation.ingredientSlots;
        EntityRef resultItem = craftingStationResult.craftOne(workstation,
                craftingStation.upgradeSlots + craftingStation.toolSlots, craftingStation.ingredientSlots,
                craftingStation.upgradeSlots, craftingStation.toolSlots,
                resultSlot);
        if (resultItem != null) {
            GiveItemAction giveItem = new GiveItemAction(workstation, resultItem, resultSlot);
            workstation.send(giveItem);
            if (!giveItem.isConsumed()) {
                resultItem.destroy();
            }
        }
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String resultId) {
        return 0;
    }
}
