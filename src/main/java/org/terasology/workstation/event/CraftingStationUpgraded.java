package org.terasology.workstation.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationUpgraded implements Event {
    private EntityRef craftingStation;

    public CraftingStationUpgraded(EntityRef craftingStation) {
        this.craftingStation = craftingStation;
    }

    public EntityRef getCraftingStation() {
        return craftingStation;
    }
}
