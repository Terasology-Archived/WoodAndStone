package org.terasology.machines.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.machines.component.MachineComponent;
import org.terasology.machines.event.MachineStateChanged;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class InventoryBasedMachineSystem extends BaseComponentSystem {
    @ReceiveEvent
    public void newItemInMachine(InventorySlotChangedEvent event, EntityRef workstation, MachineComponent machine) {
        workstation.send(new MachineStateChanged());
    }

    @ReceiveEvent
    public void itemCountChangedInMachine(InventorySlotStackSizeChangedEvent event, EntityRef workstation, MachineComponent machine) {
        workstation.send(new MachineStateChanged());
    }
}
