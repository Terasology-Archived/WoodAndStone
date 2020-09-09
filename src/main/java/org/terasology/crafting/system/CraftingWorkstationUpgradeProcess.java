// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system;

import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.crafting.component.CraftingStationUpgradeRecipeComponent;
import org.terasology.crafting.event.CraftingStationUpgraded;
import org.terasology.crafting.system.recipe.workstation.UpgradeRecipe;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.ValidateInventoryItem;

public class CraftingWorkstationUpgradeProcess implements WorkstationProcess, ValidateInventoryItem {
    private final String workstationType;
    private final String targetWorkstationType;
    private final UpgradeRecipe upgradeRecipe;

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
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "UPGRADE").contains(slotNo)) {
            CraftingStationComponent station = workstation.getComponent(CraftingStationComponent.class);
            return station != null && station.type.equals(workstationType) && upgradeRecipe.isUpgradeComponent(item);
        } else {
            return true;
        }
    }

    @Override
    public long startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcessRequest request,
                                      EntityRef processEntity) throws InvalidProcessException {
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
