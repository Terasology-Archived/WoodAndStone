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

import org.terasology.crafting.component.CraftingProcessComponent;
import org.terasology.crafting.event.CraftingWorkstationProcessRequest;
import org.terasology.crafting.system.recipe.workstation.CraftingStationRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.process.fluid.ValidateFluidInventoryItem;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

import java.util.List;

public class CraftingWorkstationProcess implements WorkstationProcess, ValidateInventoryItem, ValidateFluidInventoryItem {
    private String processType;
    private String craftingRecipeId;
    private CraftingStationRecipe recipe;

    public CraftingWorkstationProcess(String processType, String craftingRecipeId, CraftingStationRecipe recipe) {
        this.processType = processType;
        this.craftingRecipeId = craftingRecipeId;
        this.recipe = recipe;
    }

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        return WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT").contains(slotNo)
                || WorkstationInventoryUtils.getAssignedSlots(workstation, "TOOL").contains(slotNo)
                || WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT").contains(slotNo);
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT").contains(slotNo)) {
            return recipe.hasAsComponent(item);
        }
        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "TOOL").contains(slotNo)) {
            return recipe.hasAsTool(item);
        }
        return instigator == workstation;
    }

    @Override
    public boolean isResponsibleForFluidSlot(EntityRef workstation, int slotNo) {
        return WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT").contains(slotNo);
    }

    @Override
    public boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
        return recipe.hasFluidAsComponent(fluidType);
    }

    @Override
    public String getProcessType() {
        return processType;
    }

    @Override
    public String getId() {
        return craftingRecipeId;
    }

    public CraftingStationRecipe getCraftingWorkstationRecipe() {
        return recipe;
    }

    @Override
    public long startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcessRequest request, EntityRef processEntity) throws InvalidProcessException {
        if (!(request instanceof CraftingWorkstationProcessRequest)) {
            throw new InvalidProcessException();
        }

        final CraftingWorkstationProcessRequest craftingRequest = (CraftingWorkstationProcessRequest) request;
        final List<String> parameters = craftingRequest.getParameters();
        final CraftingStationRecipe.CraftingStationResult result = recipe.getResultByParameters(workstation, parameters);
        if (result == null) {
            throw new InvalidProcessException();
        }

        final int count = craftingRequest.getCount();
        final boolean success = result.startCrafting(workstation, count);
        if (!success) {
            throw new InvalidProcessException();
        }

        CraftingProcessComponent craftingProcess = new CraftingProcessComponent();
        craftingProcess.parameters = parameters;
        craftingProcess.count = count;
        processEntity.addComponent(craftingProcess);

        return result.getProcessDuration();
    }

    @Override
    public long startProcessingAutomatic(EntityRef workstation, EntityRef processEntity) throws InvalidProcessException {
        throw new InvalidProcessException();
    }

    @Override
    public void finishProcessing(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        CraftingProcessComponent craftingProcess = processEntity.getComponent(CraftingProcessComponent.class);

        final CraftingStationRecipe.CraftingStationResult result = recipe.getResultByParameters(workstation, craftingProcess.parameters);
        EntityRef resultItem = result.finishCrafting(workstation, craftingProcess.count);
        GiveItemAction giveItem = new GiveItemAction(workstation, resultItem, WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT"));
        workstation.send(giveItem);
        if (giveItem.isConsumed()) {
            return;
        }
        resultItem.destroy();
    }
}
