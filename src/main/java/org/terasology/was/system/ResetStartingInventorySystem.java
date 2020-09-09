// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.system;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.inventory.logic.InventoryComponent;
import org.terasology.inventory.logic.InventoryManager;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class ResetStartingInventorySystem extends BaseComponentSystem {
    @In
    public EntityRef player;
    @In
    private EntityManager entityManager;
    @In
    private InventoryManager manager;

    @ReceiveEvent(components = {InventoryComponent.class, CharacterComponent.class}, priority =
            EventPriority.PRIORITY_TRIVIAL)
    //@ReceiveEvent()
    public void resetStartingInventory(OnPlayerSpawnedEvent event, EntityRef character) {
        for (int i = 0; i < manager.getNumSlots(character); i++) {
            manager.removeItem(character, EntityRef.NULL, manager.getItemInSlot(character, i), true);
        }
    }
}
