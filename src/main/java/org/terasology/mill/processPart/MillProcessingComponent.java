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
package org.terasology.mill.processPart;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.mill.component.MillProcessedComponent;
import org.terasology.mill.component.MillProgressComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class MillProcessingComponent implements Component, ProcessPart, ValidateInventoryItem {
    private static final int MILL_STEP_COUNT = 4;

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
            return item.hasComponent(MillProcessedComponent.class);
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
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException {
        if (!workstation.hasComponent(WorkstationInventoryComponent.class)) {
            throw new InvalidProcessException();
        }

        if (workstation.hasComponent(MillProgressComponent.class)) {
            return Collections.singleton("progress");
        }

        Set<String> result = new LinkedHashSet<>();
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            MillProcessedComponent processed = InventoryUtils.getItemAt(workstation, slot).getComponent(MillProcessedComponent.class);
            if (processed != null) {
                appendResultIfCanStore(workstation, result, slot, getResult(processed));
            }
        }

        if (result.size() > 0) {
            return result;
        } else {
            throw new InvalidProcessException();
        }
    }

    private String getResult(MillProcessedComponent processed) {
        return processed.blockResult != null ? processed.blockResult : processed.itemResult;
    }

    private void appendResultIfCanStore(EntityRef workstation, Set<String> result, int slot, String resultObject) {
        EntityRef resultItem = createResultItem(resultObject);
        try {
            for (int outputSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
                if (InventoryUtils.canStackInto(resultItem, InventoryUtils.getItemAt(workstation, outputSlot))) {
                    result.add(String.valueOf(slot));
                    return;
                }
            }
        } finally {
            resultItem.destroy();
        }
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result) {
        MillProcessedComponent processed;
        MillProgressComponent progress = workstation.getComponent(MillProgressComponent.class);
        if (progress != null) {
            processed = progress.processedItem.getComponent(MillProcessedComponent.class);
        } else {
            processed = InventoryUtils.getItemAt(workstation, Integer.parseInt(result)).getComponent(MillProcessedComponent.class);
        }

        return processed.millLength / MILL_STEP_COUNT;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result) {
        if (!result.equals("progress")) {
            EntityRef item = InventoryUtils.getItemAt(workstation, Integer.parseInt(result));
            RemoveItemAction removeItem = new RemoveItemAction(instigator, item, false, 1);
            workstation.send(removeItem);
            EntityRef removedItem = removeItem.getRemovedItem();
            MillProgressComponent millProgress = new MillProgressComponent();
            millProgress.processedItem = removedItem;
            workstation.addComponent(millProgress);
        }
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result) {
        MillProgressComponent millProgress = workstation.getComponent(MillProgressComponent.class);
        millProgress.processedStep++;
        if (millProgress.processedStep < MILL_STEP_COUNT) {
            workstation.saveComponent(millProgress);
        } else {
            workstation.removeComponent(MillProgressComponent.class);

            MillProcessedComponent processed = millProgress.processedItem.getComponent(MillProcessedComponent.class);
            EntityRef resultItem = createResultItem(getResult(processed));
            millProgress.processedItem.destroy();

            for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
                GiveItemAction action = new GiveItemAction(instigator, resultItem, slot);
                workstation.send(action);
                if (action.isConsumed()) {
                    return;
                }
            }
            resultItem.destroy();
        }
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
