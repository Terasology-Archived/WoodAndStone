/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.system.recipe.hand.behaviour;

import org.terasology.crafting.component.CraftInHandRecipeComponent;
import org.terasology.crafting.system.recipe.hand.ItemCraftBehaviour;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.RemoveItemAction;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConsumeItemCraftBehaviour implements ItemCraftBehaviour {
    private String itemType;
    private int count;

    public ConsumeItemCraftBehaviour(String itemType) {
        this(itemType, 1);
    }

    public ConsumeItemCraftBehaviour(String itemType, int count) {
        this.itemType = itemType;
        this.count = count;
    }

    @Override
    public boolean isValid(EntityRef character, EntityRef item) {
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        CraftInHandRecipeComponent craftComponent = item.getComponent(CraftInHandRecipeComponent.class);
        return craftComponent != null && craftComponent.componentType.equals(itemType)
                && itemComponent != null && itemComponent.stackCount >= count;
    }

    @Override
    public int getCountToDisplay() {
        return count;
    }

    @Override
    public void processForItem(EntityRef character, EntityRef item) {
        RemoveItemAction removeAction = new RemoveItemAction(character, item, true, count);
        character.send(removeAction);
    }
}
