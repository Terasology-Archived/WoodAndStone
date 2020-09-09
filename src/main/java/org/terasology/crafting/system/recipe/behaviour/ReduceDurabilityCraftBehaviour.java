// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.behaviour;

import com.google.common.base.Predicate;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.FixedFunction;
import org.terasology.crafting.system.recipe.render.ItemSlotIngredientRenderer;
import org.terasology.durability.DurabilityComponent;
import org.terasology.durability.ReduceDurabilityEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.inventory.logic.InventoryUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ReduceDurabilityCraftBehaviour implements IngredientCraftBehaviour<EntityRef> {
    private final Predicate<EntityRef> matcher;
    private final int durabilityUsed;
    private final InventorySlotResolver resolver;

    public ReduceDurabilityCraftBehaviour(Predicate<EntityRef> matcher, int durabilityUsed,
                                          InventorySlotResolver resolver) {
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
            return durability != null && durability.durability >= durabilityUsed * multiplier;
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
