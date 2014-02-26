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
import org.terasology.crafting.system.recipe.render.FixedFunction;
import org.terasology.crafting.system.recipe.render.ItemSlotIngredientRenderer;
import org.terasology.durability.DurabilityComponent;
import org.terasology.durability.ReduceDurabilityEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ReduceDurabilityCraftBehaviour implements IngredientCraftBehaviour<EntityRef> {
    private Predicate<EntityRef> matcher;
    private int durabilityUsed;
    private InventorySlotResolver resolver;

    public ReduceDurabilityCraftBehaviour(Predicate<EntityRef> matcher, int durabilityUsed, InventorySlotResolver resolver) {
        this.matcher = matcher;
        this.durabilityUsed = durabilityUsed;
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
            DurabilityComponent durability = ingredient.getComponent(DurabilityComponent.class);
            if (durability != null && durability.durability >= durabilityUsed * multiplier) {
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
        DurabilityComponent durability = ingredient.getComponent(DurabilityComponent.class);

        return durability.durability / durabilityUsed;
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, String slot) {
        ItemSlotIngredientRenderer renderer = new ItemSlotIngredientRenderer();
        renderer.update(entity, Integer.parseInt(slot), new FixedFunction(1));
        return renderer;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, String slot, int multiplier) {
        InventoryUtils.getItemAt(entity, Integer.parseInt(slot)).send(new ReduceDurabilityEvent(multiplier));
    }
}
