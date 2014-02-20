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
package org.terasology.crafting.system.recipe.behaviour;

import com.google.common.base.Predicate;
import org.terasology.durability.DurabilityComponent;
import org.terasology.durability.ReduceDurabilityEvent;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ReduceDurabilityCraftBehaviour implements ItemCraftBehaviour {
    private Predicate<EntityRef> matcher;
    private int durabilityUsed;

    public ReduceDurabilityCraftBehaviour(Predicate<EntityRef> matcher, int durabilityUsed) {
        this.matcher = matcher;
        this.durabilityUsed = durabilityUsed;
    }

    @Override
    public boolean isValidAnyNumber(EntityRef item) {
        return matcher.apply(item);
    }

    @Override
    public boolean isValid(EntityRef item, int multiplier) {
        if (!matcher.apply(item)) {
            return false;
        }

        DurabilityComponent durability = item.getComponent(DurabilityComponent.class);
        if (durability == null) {
            return false;
        }

        return durability.durability >= durabilityUsed * multiplier;
    }

    @Override
    public int getMaxMultiplier(EntityRef item) {
        DurabilityComponent durability = item.getComponent(DurabilityComponent.class);

        return durability.durability / durabilityUsed;
    }

    @Override
    public int getCountToDisplay(int multiplier) {
        return 1;
    }

    @Override
    public void processForItem(EntityRef instigator, EntityRef inventory, EntityRef item, int multiplier) {
        item.send(new ReduceDurabilityEvent(multiplier));
    }
}
