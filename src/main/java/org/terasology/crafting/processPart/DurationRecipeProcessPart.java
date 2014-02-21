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
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.fluid.ValidateFluidInventoryItem;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

import java.util.Set;

public class DurationRecipeProcessPart implements ProcessPart, ValidateInventoryItem, ValidateFluidInventoryItem {
    private CraftingStationRecipe craftingStationRecipe;

    public DurationRecipeProcessPart(CraftingStationRecipe craftingStationRecipe) {
        this.craftingStationRecipe = craftingStationRecipe;
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result, String parameter) {
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation, String parameter) throws InvalidProcessException {
        return null;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result, String parameter) {
        return craftingStationRecipe.getResultById(result).getProcessDuration();
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result, String parameter) {
    }

    @Override
    public boolean isResponsibleForFluidSlot(EntityRef workstation, int slotNo) {
        return false;
    }

    @Override
    public boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
        return false;
    }

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        return false;
    }
}
