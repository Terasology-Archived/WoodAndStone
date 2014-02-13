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
package org.terasology.was.heat.processParts;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.registry.CoreRegistry;
import org.terasology.was.heat.HeatProcessedComponent;
import org.terasology.was.heat.HeatUtils;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class HeatProcessingComponent implements Component, ProcessPart {
    private Collection<Integer> getInputSlots(EntityRef workstation) {
        WorkstationInventoryComponent inventory = workstation.getComponent(WorkstationInventoryComponent.class);
        return Collections.unmodifiableCollection(inventory.slotAssignments.get("INPUT"));
    }

    private Collection<Integer> getOutputSlots(EntityRef workstation) {
        WorkstationInventoryComponent inventory = workstation.getComponent(WorkstationInventoryComponent.class);
        return Collections.unmodifiableCollection(inventory.slotAssignments.get("OUTPUT"));
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException {
        if (!workstation.hasComponent(WorkstationInventoryComponent.class)) {
            throw new InvalidProcessException();
        }

        float heat = HeatUtils.calculateHeatForEntity(workstation, CoreRegistry.get(BlockEntityRegistry.class));

        Set<String> result = new LinkedHashSet<>();
        for (int slot : getInputSlots(workstation)) {
            HeatProcessedComponent processed = InventoryUtils.getItemAt(workstation, slot).getComponent(HeatProcessedComponent.class);
            if (processed != null) {
                float heatRequired = processed.heatRequired;
                if (heatRequired <= heat) {

                    appendResultIfCanStore(workstation, result, slot, processed.blockResult != null ? processed.blockResult : processed.itemResult);
                }
            }
        }

        if (result.size() > 0) {
            return result;
        } else {
            throw new InvalidProcessException();
        }
    }

    private void appendResultIfCanStore(EntityRef workstation, Set<String> result, int slot, String resultObject) {
        EntityRef resultItem = createResultItem(resultObject);
        try {
            for (int outputSlot : getOutputSlots(workstation)) {
                if (InventoryUtils.canStackInto(resultItem, InventoryUtils.getItemAt(workstation, outputSlot))) {
                    result.add(slot + "|" + resultObject);
                    return;
                }
            }
        } finally {
            resultItem.destroy();
        }
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result) {
        String[] split = result.split("\\|");
        HeatProcessedComponent component = InventoryUtils.getItemAt(workstation, Integer.parseInt(split[0])).getComponent(HeatProcessedComponent.class);

        return component.processingTime;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result) {
        String[] split = result.split("\\|");
        EntityRef item = InventoryUtils.getItemAt(workstation, Integer.parseInt(split[0]));
        workstation.send(new RemoveItemAction(instigator, item, true, 1));
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result) {
        EntityRef toGive = createResultItem(result);

        for (int slot : getOutputSlots(workstation)) {
            GiveItemAction action = new GiveItemAction(instigator, toGive, slot);
            workstation.send(action);
            if (action.isConsumed()) {
                return;
            }
        }
    }

    private EntityRef createResultItem(String itemResult) {
        String[] split = itemResult.split("\\|");
        String resultItem = split[1];
        int count = 1;
        int starIndex = resultItem.indexOf("*");
        if (starIndex > -1) {
            count = Integer.parseInt(resultItem.substring(0, starIndex));
            resultItem = resultItem.substring(starIndex + 1);
        }

        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        PrefabManager prefabManager = CoreRegistry.get(PrefabManager.class);
        Prefab prefab = prefabManager.getPrefab(resultItem);

        EntityRef result;
        if (prefab != null) {
            result = entityManager.create(prefab);
            ItemComponent item = result.getComponent(ItemComponent.class);
            item.stackCount = (byte) count;
            result.saveComponent(item);
        } else {
            BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
            BlockFamily blockFamily = blockManager.getBlockFamily(resultItem);
            result = blockItemFactory.newInstance(blockFamily, count);
        }
        return result;
    }
}
