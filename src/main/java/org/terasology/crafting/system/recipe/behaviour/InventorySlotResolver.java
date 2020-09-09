// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.behaviour;

import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;

public interface InventorySlotResolver {
    List<Integer> getSlots(EntityRef entity);
}
