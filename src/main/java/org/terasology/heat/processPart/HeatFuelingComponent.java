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

import org.terasology.engine.Time;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.heat.component.HeatFuelComponent;
import org.terasology.heat.component.HeatProducerComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.component.SpecificInputSlotComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.event.WorkstationStateChanged;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

import java.util.LinkedHashSet;
import java.util.Set;

public class HeatFuelingComponent implements Component, ProcessPart, ValidateInventoryItem {
    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FUEL")) {
            if (slot == slotNo) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        return item.hasComponent(HeatFuelComponent.class);
    }

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        if (!workstation.hasComponent(WorkstationInventoryComponent.class)) {
            return false;
        }

        Set<String> result = new LinkedHashSet<>();
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FUEL")) {
            HeatFuelComponent fuel = InventoryUtils.getItemAt(workstation, slot).getComponent(HeatFuelComponent.class);
            if (fuel != null) {
                processEntity.addComponent(new SpecificInputSlotComponent(slot));
                return true;
            }
        }

        return false;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        HeatFuelComponent fuel = InventoryUtils.getItemAt(workstation, input.slot).getComponent(HeatFuelComponent.class);
        if (fuel != null) {
            return fuel.consumeTime;
        }
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        EntityRef item = InventoryUtils.getItemAt(workstation, input.slot);
        HeatFuelComponent fuel = item.getComponent(HeatFuelComponent.class);

        long time = CoreRegistry.get(Time.class).getGameTimeInMs();

        if (CoreRegistry.get(InventoryManager.class).removeItem(workstation, instigator, item, true, 1) != null) {
            HeatProducerComponent producer = workstation.getComponent(HeatProducerComponent.class);
            HeatProducerComponent.FuelSourceConsume fuelSource = new HeatProducerComponent.FuelSourceConsume();
            fuelSource.startTime = time;
            fuelSource.burnLength = fuel.consumeTime;
            fuelSource.heatProvided = fuel.heatProvided;
            producer.fuelConsumed.add(fuelSource);
            workstation.saveComponent(producer);
        }
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        workstation.send(new WorkstationStateChanged());
    }
}
