package org.terasology.was.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class ResetStartingInventorySystem implements ComponentSystem {
    @In
    private SlotBasedInventoryManager manager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void resetStartingInventory(OnPlayerSpawnedEvent event, EntityRef character) {
        for (int i = 0; i < manager.getNumSlots(character); i++) {
            manager.destroyItem(character, manager.getItemInSlot(character, i));
        }
    }
}
