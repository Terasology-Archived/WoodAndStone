// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.hand;

import org.terasology.crafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class PlayerInventorySlotResolver implements InventorySlotResolver {
    private static final PlayerInventorySlotResolver SINGLETON = new PlayerInventorySlotResolver();

    private final List<Integer> result = new LinkedList<>();

    private PlayerInventorySlotResolver() {
        for (int i = 0; i < 40; i++) {
            result.add(i);
        }
    }

    public static PlayerInventorySlotResolver singleton() {
        return SINGLETON;
    }

    @Override
    public List<Integer> getSlots(EntityRef entity) {
        return Collections.unmodifiableList(result);
    }
}
