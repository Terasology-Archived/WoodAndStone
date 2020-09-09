// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.heat.processPart;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.heat.component.HeatFuelComponent;
import org.terasology.heat.component.HeatProducerComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.InventoryUtils;
import org.terasology.workstation.component.SpecificInputSlotComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.event.WorkstationStateChanged;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityGetDurationEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;

@RegisterSystem
public class HeatFuelingProcessPartCommonSystem extends BaseComponentSystem {
    @In
    InventoryManager inventoryManager;
    @In
    Time time;

    ///// Processing

    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         HeatFuelingComponent heatFuelingComponent) {
        if (event.getWorkstation().hasComponent(WorkstationInventoryComponent.class)) {
            for (int slot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FUEL")) {
                HeatFuelComponent fuel =
                        InventoryUtils.getItemAt(event.getWorkstation(), slot).getComponent(HeatFuelComponent.class);
                if (fuel != null) {
                    processEntity.addComponent(new SpecificInputSlotComponent(slot));
                    return;
                }
            }
        }

        event.consume();
    }

    @ReceiveEvent
    public void startExecution(ProcessEntityStartExecutionEvent event, EntityRef processEntity,
                               HeatFuelingComponent heatFuelingComponent) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        EntityRef item = InventoryUtils.getItemAt(event.getWorkstation(), input.slot);
        HeatFuelComponent fuel = item.getComponent(HeatFuelComponent.class);

        long startTime = time.getGameTimeInMs();

        if (inventoryManager.removeItem(event.getWorkstation(), event.getInstigator(), item, true, 1) != null) {
            HeatProducerComponent producer = event.getWorkstation().getComponent(HeatProducerComponent.class);
            HeatProducerComponent.FuelSourceConsume fuelSource = new HeatProducerComponent.FuelSourceConsume();
            fuelSource.startTime = startTime;
            fuelSource.burnLength = fuel.consumeTime;
            fuelSource.heatProvided = fuel.heatProvided;
            producer.fuelConsumed.add(fuelSource);
            event.getWorkstation().saveComponent(producer);
        }
    }

    @ReceiveEvent
    public void getDuration(ProcessEntityGetDurationEvent event, EntityRef processEntity,
                            HeatFuelingComponent heatFuelingComponent) {
        SpecificInputSlotComponent input = processEntity.getComponent(SpecificInputSlotComponent.class);
        HeatFuelComponent fuel =
                InventoryUtils.getItemAt(event.getWorkstation(), input.slot).getComponent(HeatFuelComponent.class);
        if (fuel != null) {
            event.add(fuel.consumeTime / 1000f);
        }
    }

    @ReceiveEvent
    public void finishExecution(ProcessEntityFinishExecutionEvent event, EntityRef entityRef,
                                HeatFuelingComponent heatFuelingComponent) {
        event.getWorkstation().send(new WorkstationStateChanged());
    }

    ///// Inventory

    @ReceiveEvent
    public void isValidInventoryItem(ProcessEntityIsInvalidForInventoryItemEvent event, EntityRef processEntity,
                                     HeatFuelingComponent heatFuelingComponent) {
        if (WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FUEL").contains(event.getSlotNo())
                && !event.getItem().hasComponent(HeatFuelComponent.class)) {
            event.consume();
        }
    }
}
