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
package org.terasology.mill.processPart;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.inventory.InventoryManager;
import org.terasology.engine.logic.inventory.InventoryUtils;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.mill.component.MillProcessedComponent;
import org.terasology.mill.component.MillProgressComponent;
import org.terasology.workstation.component.SpecificInputSlotComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityGetDurationEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;

@RegisterSystem
public class MillProcessingProcessPartCommonSystem extends BaseComponentSystem {
    private static final int MILL_STEP_COUNT = 4;

    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;
    @In
    BlockManager blockManager;
    @In
    PrefabManager prefabManager;

    ///// Processing

    @ReceiveEvent
    public void validateProcess(ProcessEntityIsInvalidEvent event, EntityRef processEntity,
                                MillProcessingComponent millProcessingComponent) {
    }

    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         MillProcessingComponent millProcessingComponent) {
        if (event.getWorkstation().hasComponent(WorkstationInventoryComponent.class)) {

            if (event.getWorkstation().hasComponent(MillProgressComponent.class)) {
                return;
            }

            for (int slot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "INPUT")) {
                MillProcessedComponent processed = InventoryUtils.getItemAt(event.getWorkstation(), slot).getComponent(MillProcessedComponent.class);
                if (processed != null) {
                    if (canOutputResult(event.getWorkstation(), getResult(processed))) {
                        processEntity.addComponent(new SpecificInputSlotComponent(slot));
                        return;
                    }
                }
            }
        }

        event.consume();
    }

    @ReceiveEvent
    public void startExecution(ProcessEntityStartExecutionEvent event, EntityRef processEntity,
                               MillProcessingComponent millProcessingComponent) {
        if (!event.getWorkstation().hasComponent(MillProgressComponent.class)) {
            SpecificInputSlotComponent specificInputSlotComponent = processEntity.getComponent(SpecificInputSlotComponent.class);
            EntityRef item = InventoryUtils.getItemAt(event.getWorkstation(), specificInputSlotComponent.slot);
            final EntityRef removedItem = inventoryManager.removeItem(event.getWorkstation(), event.getInstigator(), item, false, 1);
            if (removedItem != null) {
                MillProgressComponent millProgress = new MillProgressComponent();
                millProgress.processedItem = removedItem;
                event.getWorkstation().addComponent(millProgress);
            }
        }
    }

    @ReceiveEvent
    public void getDuration(ProcessEntityGetDurationEvent event, EntityRef processEntity,
                            MillProcessingComponent millProcessingComponent) {
        MillProcessedComponent processed;
        MillProgressComponent progress = event.getWorkstation().getComponent(MillProgressComponent.class);
        if (progress != null) {
            processed = progress.processedItem.getComponent(MillProcessedComponent.class);
        } else {
            final SpecificInputSlotComponent component = processEntity.getComponent(SpecificInputSlotComponent.class);
            processed = InventoryUtils.getItemAt(event.getWorkstation(), component.slot).getComponent(MillProcessedComponent.class);
        }

        event.add(processed.millLength / MILL_STEP_COUNT / 1000f);
    }

    @ReceiveEvent
    public void finishExecution(ProcessEntityFinishExecutionEvent event, EntityRef entityRef,
                                MillProcessingComponent millProcessingComponent) {
        MillProgressComponent millProgress = event.getWorkstation().getComponent(MillProgressComponent.class);
        millProgress.processedStep++;
        if (millProgress.processedStep < MILL_STEP_COUNT) {
            event.getWorkstation().saveComponent(millProgress);
        } else {
            event.getWorkstation().removeComponent(MillProgressComponent.class);

            MillProcessedComponent processed = millProgress.processedItem.getComponent(MillProcessedComponent.class);
            EntityRef resultItem = createResultItem(getResult(processed));
            millProgress.processedItem.destroy();

            if (inventoryManager.giveItem(event.getWorkstation(), event.getInstigator(), resultItem, WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "OUTPUT"))) {
                return;
            }
            resultItem.destroy();
        }
    }

    ///// Inventory

    @ReceiveEvent
    public void isValidInventoryItem(ProcessEntityIsInvalidForInventoryItemEvent event, EntityRef processEntity,
                                     MillProcessingComponent millProcessingComponent) {
        if (WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "INPUT").contains(event.getSlotNo())
                && !event.getItem().hasComponent(MillProcessedComponent.class)) {
            event.consume();
        }

        if (WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "OUTPUT").contains(event.getSlotNo())
                && event.getWorkstation() != event.getInstigator()) {
            event.consume();
        }
    }

    private String getResult(MillProcessedComponent processed) {
        return processed.blockResult != null ? processed.blockResult : processed.itemResult;
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
