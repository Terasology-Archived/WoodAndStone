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
package org.terasology.heat.processPart;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.heat.HeatUtils;
import org.terasology.heat.component.HeatProcessedComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.component.OutputTypeComponent;
import org.terasology.workstation.component.SpecificInputSlotComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

public class HeatProcessingComponent implements Component, ProcessPart, ValidateInventoryItem {
    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        if (isInputSlot(workstation, slotNo)) {
            return true;
        }
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        if (isInputSlot(workstation, slotNo)) {
            return item.hasComponent(HeatProcessedComponent.class);
        }
        return workstation == instigator;
    }

    private boolean isInputSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        if (!workstation.hasComponent(WorkstationInventoryComponent.class)) {
            return false;
        }

        // Defer the heat calculation until it is actually needed
        Float heat = null;

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            HeatProcessedComponent processed = InventoryUtils.getItemAt(workstation, slot).getComponent(HeatProcessedComponent.class);
            if (processed != null) {
                float heatRequired = processed.heatRequired;
                if (heat == null) {
                    heat = HeatUtils.calculateHeatForEntity(workstation, CoreRegistry.get(BlockEntityRegistry.class));
                }
                if (heatRequired <= heat) {
                    final String result = processed.blockResult != null ? processed.blockResult : processed.itemResult;
                    if (canOutputResult(workstation, result)) {
                        processEntity.addComponent(new SpecificInputSlotComponent(slot));
                        processEntity.addComponent(new OutputTypeComponent(result));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean canOutputResult(EntityRef workstation, String resultObject) {
        EntityRef resultItem = createResultItem(resultObject);
        try {
            for (int outputSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
                if (InventoryUtils.canStackInto(resultItem, InventoryUtils.getItemAt(workstation, outputSlot))) {
                    return true;
                }
            }
        } finally {
            resultItem.destroy();
        }
        return false;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        HeatProcessedComponent component = InventoryUtils.getItemAt(workstation, input.slot).getComponent(HeatProcessedComponent.class);

        return component.processingTime;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        EntityRef item = InventoryUtils.getItemAt(workstation, input.slot);
        CoreRegistry.get(InventoryManager.class).removeItem(workstation, instigator, item, true, 1);
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        OutputTypeComponent output = processEntity.getComponent(OutputTypeComponent.class);
        EntityRef toGive = createResultItem(output.type);

        if (CoreRegistry.get(InventoryManager.class).giveItem(workstation, instigator, toGive, WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT"))) {
            return;
        }
        toGive.destroy();
    }

    private EntityRef createResultItem(String itemResult) {
        int count = 1;
        int starIndex = itemResult.indexOf("*");
        if (starIndex > -1) {
            count = Integer.parseInt(itemResult.substring(0, starIndex));
            itemResult = itemResult.substring(starIndex + 1);
        }

        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        PrefabManager prefabManager = CoreRegistry.get(PrefabManager.class);
        Prefab prefab = prefabManager.getPrefab(itemResult);

        EntityRef result;
        if (prefab != null) {
            result = entityManager.create(prefab);
            ItemComponent item = result.getComponent(ItemComponent.class);
            item.stackCount = (byte) count;
            result.saveComponent(item);
        } else {
            BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
            BlockFamily blockFamily = blockManager.getBlockFamily(itemResult);
            result = blockItemFactory.newInstance(blockFamily, count);
        }
        return result;
    }
}
