// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system;

import org.terasology.crafting.component.CraftingProcessComponent;
import org.terasology.crafting.event.CraftingWorkstationProcessRequest;
import org.terasology.crafting.system.recipe.workstation.CraftingStationRecipe;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.process.fluid.ValidateFluidInventoryItem;
import org.terasology.workstation.system.ValidateInventoryItem;

import java.util.List;

public class CraftingWorkstationProcess implements WorkstationProcess, ValidateInventoryItem,
        ValidateFluidInventoryItem {
    private final String processType;
    private final String craftingRecipeId;
    private final CraftingStationRecipe recipe;

    public CraftingWorkstationProcess(String processType, String craftingRecipeId, CraftingStationRecipe recipe) {
        this.processType = processType;
        this.craftingRecipeId = craftingRecipeId;
        this.recipe = recipe;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT").contains(slotNo)) {
            return recipe.hasAsComponent(item);
        }
        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "TOOL").contains(slotNo)) {
            return recipe.hasAsTool(item);
        }
        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT").contains(slotNo)) {
            return instigator == workstation;
        }
        return true;
    }

    @Override
    public boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT").contains(slotNo)) {
            return recipe.hasFluidAsComponent(fluidType);
        } else {
            return true;
        }
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
    public long startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcessRequest request,
                                      EntityRef processEntity) throws InvalidProcessException {
        if (!(request instanceof CraftingWorkstationProcessRequest)) {
            throw new InvalidProcessException();
        }

        final CraftingWorkstationProcessRequest craftingRequest = (CraftingWorkstationProcessRequest) request;
        final List<String> parameters = craftingRequest.getParameters();
        final CraftingStationRecipe.CraftingStationResult result = recipe.getResultByParameters(workstation,
                parameters);
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

        final CraftingStationRecipe.CraftingStationResult result = recipe.getResultByParameters(workstation,
                craftingProcess.parameters);
        EntityRef resultItem = result.finishCrafting(workstation, craftingProcess.count);
        if (CoreRegistry.get(InventoryManager.class).giveItem(workstation, workstation, resultItem,
                WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT"))) {
            return;
        }
        resultItem.destroy();
    }
}
