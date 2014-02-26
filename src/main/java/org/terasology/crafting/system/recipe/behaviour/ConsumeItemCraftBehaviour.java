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
public class ConsumeItemCraftBehaviour implements IngredientCraftBehaviour<EntityRef> {
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
    public List<String> getValidToCraft(EntityRef entity, int multiplier) {
        List<String> result = new LinkedList<>();
        for (int slot : resolver.getSlots(entity)) {
            if (isValidToCraft(entity, slot, multiplier)) {
                result.add(String.valueOf(slot));
            }
        }

        return result;
    }

    private boolean isValidToCraft(EntityRef entity, int slot, int multiplier) {
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
    public boolean isValidToCraft(EntityRef entity, String slot, int multiplier) {
        return isValidToCraft(entity, Integer.parseInt(slot), multiplier);
    }

    @Override
    public int getMaxMultiplier(EntityRef entity, String slot) {
        EntityRef ingredient = InventoryUtils.getItemAt(entity, Integer.parseInt(slot));
        ItemComponent itemComponent = ingredient.getComponent(ItemComponent.class);
        return itemComponent.stackCount / count;
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, String slot) {
        ItemSlotIngredientRenderer renderer = new ItemSlotIngredientRenderer();
        renderer.update(entity, Integer.parseInt(slot), new MultiplyFunction(count));
        return renderer;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, String slot, int multiplier) {
        RemoveItemAction removeAction = new RemoveItemAction(instigator, InventoryUtils.getItemAt(entity, Integer.parseInt(slot)), true, count * multiplier);
        entity.send(removeAction);
    }
}