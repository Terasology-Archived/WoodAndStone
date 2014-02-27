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

import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.crafting.component.CraftingStationUpgradeRecipeComponent;
import org.terasology.crafting.event.CraftingStationUpgraded;
import org.terasology.crafting.system.recipe.workstation.UpgradeRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

public class CraftingWorkstationUpgradeProcess implements WorkstationProcess, ValidateInventoryItem {
    private String workstationType;
    private String targetWorkstationType;
    private UpgradeRecipe upgradeRecipe;

    public CraftingWorkstationUpgradeProcess(String workstationType, String targetWorkstationType,
                                             UpgradeRecipe upgradeRecipe) {
        this.workstationType = workstationType;
        this.targetWorkstationType = targetWorkstationType;
        this.upgradeRecipe = upgradeRecipe;
    }

    @Override
    public String getProcessType() {
        return CraftingStationUpgradeRecipeComponent.PROCESS_TYPE;
    }

    @Override
    public String getId() {
        return "Upgrade:" + workstationType + "-" + targetWorkstationType;
    }

    public UpgradeRecipe getUpgradeRecipe() {
        return upgradeRecipe;
    }

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        return WorkstationInventoryUtils.getAssignedSlots(workstation, "UPGRADE").contains(slotNo);
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        CraftingStationComponent station = workstation.getComponent(CraftingStationComponent.class);
        return station != null && station.type.equals(workstationType) && upgradeRecipe.isUpgradeComponent(item);
    }

    @Override
    public long startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcessRequest request, EntityRef processEntity) throws InvalidProcessException {
        final UpgradeRecipe.UpgradeResult upgrade = upgradeRecipe.getMatchingUpgradeResult(workstation);
        if (upgrade == null) {
            throw new InvalidProcessException();
        }

        return 0;
    }

    @Override
    public long startProcessingAutomatic(EntityRef workstation, EntityRef processEntity) throws InvalidProcessException {
        final UpgradeRecipe.UpgradeResult upgrade = upgradeRecipe.getMatchingUpgradeResult(workstation);
        if (upgrade == null) {
            throw new InvalidProcessException();
        }

        return 0;
    }

    @Override
    public void finishProcessing(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        final UpgradeRecipe.UpgradeResult upgrade = upgradeRecipe.getMatchingUpgradeResult(workstation);
        if (upgrade != null) {
            EntityRef resultStation = upgrade.processUpgrade(workstation);
            instigator.send(new CraftingStationUpgraded(resultStation));
        }
    }
}
