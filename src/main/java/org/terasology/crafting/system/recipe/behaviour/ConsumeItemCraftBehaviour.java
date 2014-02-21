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
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.ItemSlotIngredientRenderer;
import org.terasology.crafting.system.recipe.render.MultiplyFunction;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.RemoveItemAction;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConsumeItemCraftBehaviour implements IngredientCraftBehaviour<EntityRef, Integer> {
    private Predicate<EntityRef> matcher;
    private int count;
    private InventorySlotResolver resolver;

    public ConsumeItemCraftBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
        this.matcher = matcher;
        this.count = count;
        this.resolver = resolver;
    }

    @Override
    public boolean isValidAnyAmount(EntityRef ingredient) {
        return matcher.apply(ingredient);
    }

    @Override
    public List<Integer> getValidToCraft(EntityRef entity, int multiplier) {
        List<Integer> result = new LinkedList<>();
        for (int slot : resolver.getSlots(entity)) {
            if (isValidToCraft(entity, slot, multiplier)) {
                result.add(slot);
            }
        }

        return result;
    }

    @Override
    public boolean isValidToCraft(EntityRef entity, Integer slot, int multiplier) {
        EntityRef ingredient = InventoryUtils.getItemAt(entity, slot);
        if (matcher.apply(ingredient)) {
            ItemComponent itemComponent = ingredient.getComponent(ItemComponent.class);
            if (itemComponent != null && itemComponent.stackCount >= count * multiplier) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getMaxMultiplier(EntityRef entity, Integer slot) {
        EntityRef ingredient = InventoryUtils.getItemAt(entity, slot);
        ItemComponent itemComponent = ingredient.getComponent(ItemComponent.class);
        return itemComponent.stackCount / count;
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, Integer slot) {
        ItemSlotIngredientRenderer renderer = new ItemSlotIngredientRenderer();
        renderer.update(entity, slot, new MultiplyFunction(count));
        return renderer;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, Integer slot, int multiplier) {
        RemoveItemAction removeAction = new RemoveItemAction(instigator, InventoryUtils.getItemAt(entity, slot), true, count * multiplier);
        entity.send(removeAction);
    }
}