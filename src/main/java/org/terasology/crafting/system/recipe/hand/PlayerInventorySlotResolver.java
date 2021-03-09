/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.system.recipe.hand;

import org.terasology.crafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class PlayerInventorySlotResolver implements InventorySlotResolver {
    private static final PlayerInventorySlotResolver SINGLETON = new PlayerInventorySlotResolver();

    private List<Integer> result = new LinkedList<>();

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
