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
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.inventory.ValidateFluidInventoryItem;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;
import org.terasology.workstation.process.inventory.WorkstationInventoryUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidateRecipeProcessPart implements ProcessPart, ValidateInventoryItem, ValidateFluidInventoryItem {
    private CraftingStationRecipe craftingStationRecipe;

    public ValidateRecipeProcessPart(CraftingStationRecipe craftingStationRecipe) {
        this.craftingStationRecipe = craftingStationRecipe;
    }

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "TOOL")) {
            if (slot == slotNo) {
                return true;
            }
        }

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean isResponsibleForFluidSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            if (slot == slotNo) {
                return craftingStationRecipe.hasAsComponent(item);
            }
        }

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "TOOL")) {
            if (slot == slotNo) {
                return craftingStationRecipe.hasAsTool(item);
            }
        }

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
            if (slot == slotNo) {
                return instigator == workstation;
            }
        }

        return false;
    }

    @Override
    public boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT")) {
            if (slot == slotNo) {
                return craftingStationRecipe.hasFluidAsComponent(fluidType);
            }
        }

        return false;
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException {
        CraftingStationComponent craftingStation = workstation.getComponent(CraftingStationComponent.class);
        if (craftingStation == null) {
            throw new InvalidProcessException();
        }
        List<CraftingStationRecipe.CraftingStationResult> result = craftingStationRecipe.getMatchingRecipeResults(workstation);

        Set<String> resultIds = new HashSet<>();
        for (CraftingStationRecipe.CraftingStationResult craftingStationResult : result) {
            resultIds.add(craftingStationResult.getResultId());
        }

        if (resultIds.size() == 0) {
            throw new InvalidProcessException();
        }

        return resultIds;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String resultId) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String resultId) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String resultId) {
    }
}
