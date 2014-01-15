package org.terasology.was.system;

import org.terasology.core.logic.BeforeGivingStartingInventory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class ResetStartingInventorySystem implements ComponentSystem {
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void resetStartingInventory(BeforeGivingStartingInventory event, EntityRef character) {
        event.consume();
    }
}
