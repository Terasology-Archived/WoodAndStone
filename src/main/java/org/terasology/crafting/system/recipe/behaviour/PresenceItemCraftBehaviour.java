// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.behaviour;

import com.google.common.base.Predicate;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.FixedFunction;
import org.terasology.crafting.system.recipe.render.ItemSlotIngredientRenderer;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.inventory.logic.InventoryUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PresenceItemCraftBehaviour implements IngredientCraftBehaviour<EntityRef> {
    private final Predicate<EntityRef> matcher;
    private final int count;
    private final InventorySlotResolver resolver;

    public PresenceItemCraftBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
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
            return itemComponent != null && itemComponent.stackCount >= count * multiplier;
        }
        return false;
    }

    @Override
    public boolean isValidToCraft(EntityRef entity, String slot, int multiplier) {
        return isValidToCraft(entity, Integer.parseInt(slot), multiplier);
    }

    @Override
    public int getMaxMultiplier(EntityRef entity, String slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, String slot) {
        ItemSlotIngredientRenderer renderer = new ItemSlotIngredientRenderer();
        renderer.update(entity, Integer.parseInt(slot), new FixedFunction(count));
        return renderer;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, String slot, int multiplier) {
    }
}
