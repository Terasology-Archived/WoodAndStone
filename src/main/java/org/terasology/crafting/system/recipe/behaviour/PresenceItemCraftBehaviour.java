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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PresenceItemCraftBehaviour implements ItemCraftBehaviour {
    private Predicate<EntityRef> matcher;
    private int count;

    public PresenceItemCraftBehaviour(Predicate<EntityRef> matcher, int count) {
        this.matcher = matcher;
        this.count = count;
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
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        return itemComponent != null && itemComponent.stackCount >= count;
    }

    @Override
    public int getMaxMultiplier(EntityRef item) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getCountToDisplay(int multiplier) {
        return count;
    }

    @Override
    public void processForItem(EntityRef instigator, EntityRef inventory, EntityRef item, int multiplier) {
    }
}
