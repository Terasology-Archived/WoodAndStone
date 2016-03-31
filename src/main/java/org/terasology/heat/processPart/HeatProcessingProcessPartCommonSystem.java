/*
 * Copyright 2016 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.heat.HeatUtils;
import org.terasology.heat.component.HeatProcessedComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.In;
import org.terasology.workstation.component.OutputTypeComponent;
import org.terasology.workstation.component.SpecificInputSlotComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityGetDurationEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

@RegisterSystem
public class HeatProcessingProcessPartCommonSystem extends BaseComponentSystem {
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;
    @In
    BlockManager blockManager;
    @In
    PrefabManager prefabManager;
    @In
    BlockEntityRegistry blockEntityRegistry;

    ///// Processing

    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         HeatProcessingComponent heatProcessingComponent) {
        if (event.getWorkstation().hasComponent(WorkstationInventoryComponent.class)) {
            // Defer the heat calculation until it is actually needed
            Float heat = null;

            for (int slot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "INPUT")) {
                HeatProcessedComponent processed = InventoryUtils.getItemAt(event.getWorkstation(), slot).getComponent(HeatProcessedComponent.class);
                if (processed != null) {
                    float heatRequired = processed.heatRequired;
                    if (heat == null) {
                        heat = HeatUtils.calculateHeatForEntity(event.getWorkstation(), blockEntityRegistry);
                    }
                    if (heatRequired <= heat) {
                        final String result = processed.blockResult != null ? processed.blockResult : processed.itemResult;
                        if (canOutputResult(event.getWorkstation(), result)) {
                            processEntity.addComponent(new SpecificInputSlotComponent(slot));
                            processEntity.addComponent(new OutputTypeComponent(result));
                            return;
                        }
                    }
                }
            }
        }

        event.consume();
    }

    @ReceiveEvent
    public void startExecution(ProcessEntityStartExecutionEvent event, EntityRef processEntity,
                               HeatProcessingComponent heatProcessingComponent) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        EntityRef item = InventoryUtils.getItemAt(event.getWorkstation(), input.slot);
        inventoryManager.removeItem(event.getWorkstation(), event.getInstigator(), item, true, 1);
    }

    @ReceiveEvent
    public void getDuration(ProcessEntityGetDurationEvent event, EntityRef processEntity,
                            HeatProcessingComponent heatProcessingComponent) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        HeatProcessedComponent component = InventoryUtils.getItemAt(event.getWorkstation(), input.slot).getComponent(HeatProcessedComponent.class);
        event.add(component.processingTime / 1000f);
    }

    @ReceiveEvent
    public void finishExecution(ProcessEntityFinishExecutionEvent event, EntityRef processEntity,
                                HeatProcessingComponent heatProcessingComponent) {
        OutputTypeComponent output = processEntity.getComponent(OutputTypeComponent.class);
        EntityRef toGive = createResultItem(output.type);

        if (inventoryManager.giveItem(event.getWorkstation(), event.getInstigator(), toGive, WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "OUTPUT"))) {
            return;
        }
        toGive.destroy();
    }

    ///// Inventory

    @ReceiveEvent
    public void isValidInventoryItem(ProcessEntityIsInvalidForInventoryItemEvent event, EntityRef processEntity,
                                     HeatProcessingComponent heatProcessingComponent) {
        if (WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "INPUT").contains(event.getSlotNo())
                && !event.getItem().hasComponent(HeatProcessedComponent.class)) {
            event.consume();
        } else if (WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "OUTPUT").contains(event.getSlotNo())
                && event.getWorkstation() != event.getInstigator()) {
            event.consume();
        }
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

    private EntityRef createResultItem(String itemResult) {
        String resultText = itemResult;
        int count = 1;
        int starIndex = resultText.indexOf("*");
        if (starIndex > -1) {
            count = Integer.parseInt(resultText.substring(0, starIndex));
            resultText = resultText.substring(starIndex + 1);
        }

        Prefab prefab = prefabManager.getPrefab(resultText);

        EntityRef result;
        if (prefab != null) {
            result = entityManager.create(prefab);
            ItemComponent item = result.getComponent(ItemComponent.class);
            item.stackCount = (byte) count;
            result.saveComponent(item);
        } else {
            BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
            BlockFamily blockFamily = blockManager.getBlockFamily(resultText);
            result = blockItemFactory.newInstance(blockFamily, count);
        }
        return result;
    }
}
