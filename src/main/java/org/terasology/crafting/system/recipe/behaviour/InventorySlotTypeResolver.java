// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.behaviour;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.workstation.process.WorkstationInventoryUtils;

import java.util.List;

public class InventorySlotTypeResolver implements InventorySlotResolver {
    private final String slotType;

    public InventorySlotTypeResolver(String slotType) {
        this.slotType = slotType;
    }

    @Override
    public List<Integer> getSlots(EntityRef entity) {
        return WorkstationInventoryUtils.getAssignedSlots(entity, slotType);
    }
}
