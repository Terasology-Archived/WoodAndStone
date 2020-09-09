// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationUpgraded implements Event {
    private final EntityRef craftingStation;

    public CraftingStationUpgraded(EntityRef craftingStation) {
        this.craftingStation = craftingStation;
    }

    public EntityRef getCraftingStation() {
        return craftingStation;
    }
}
