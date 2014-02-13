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

import org.terasology.engine.Time;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.machines.event.MachineStateChanged;
import org.terasology.registry.CoreRegistry;
import org.terasology.was.heat.HeatFuelComponent;
import org.terasology.was.heat.HeatProducerComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class HeatFuelingComponent implements Component, ProcessPart {
    private Collection<Integer> getFuelSlots(EntityRef workstation) {
        WorkstationInventoryComponent inventory = workstation.getComponent(WorkstationInventoryComponent.class);
        return Collections.unmodifiableCollection(inventory.slotAssignments.get("FUEL"));
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException {
        if (!workstation.hasComponent(WorkstationInventoryComponent.class)) {
            throw new InvalidProcessException();
        }

        Set<String> result = new LinkedHashSet<>();
        for (int slot : getFuelSlots(workstation)) {
            HeatFuelComponent fuel = InventoryUtils.getItemAt(workstation, slot).getComponent(HeatFuelComponent.class);
            if (fuel != null) {
                result.add(String.valueOf(slot));
            }
        }

        if (result.size() > 0) {
            return result;
        } else {
            throw new InvalidProcessException();
        }
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result) {
        HeatFuelComponent fuel = InventoryUtils.getItemAt(workstation, Integer.parseInt(result)).getComponent(HeatFuelComponent.class);
        if (fuel != null) {
            return fuel.consumeTime;
        }
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result) {
        EntityRef item = InventoryUtils.getItemAt(workstation, Integer.parseInt(result));
        HeatFuelComponent fuel = item.getComponent(HeatFuelComponent.class);

        long time = CoreRegistry.get(Time.class).getGameTimeInMs();

        RemoveItemAction remove = new RemoveItemAction(instigator, item, true, 1);
        workstation.send(remove);
        if (remove.isConsumed()) {
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
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result) {
        workstation.send(new MachineStateChanged());
    }
}
