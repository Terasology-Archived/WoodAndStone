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
import org.terasology.crafting.system.recipe.workstation.UpgradeRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

import java.util.Set;

public class ValidateUpgradeProcessPart implements ProcessPart, ValidateInventoryItem {
    private UpgradeRecipe upgradeRecipe;

    public ValidateUpgradeProcessPart(UpgradeRecipe upgradeRecipe) {
        this.upgradeRecipe = upgradeRecipe;
    }

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "UPGRADE")) {
            if (slot == slotNo) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "UPGRADE")) {
            if (slot == slotNo) {
                return upgradeRecipe.isUpgradeComponent(item);
            }
        }

        return false;
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation, String parameter) throws InvalidProcessException {
        final CraftingStationComponent craftingStation = workstation.getComponent(CraftingStationComponent.class);
        if (craftingStation == null) {
            throw new InvalidProcessException();
        }

        final UpgradeRecipe.UpgradeResult result = upgradeRecipe.getMatchingUpgradeResult(workstation);

        if (result == null) {
            throw new InvalidProcessException();
        }

        return null;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String resultId, String parameter) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String resultId, String parameter) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String resultId, String parameter) {
    }
}
