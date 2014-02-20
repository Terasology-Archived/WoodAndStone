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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PresenceItemCraftBehaviour implements IngredientCraftBehaviour<EntityRef> {
    private Predicate<EntityRef> matcher;
    private int count;
    private ItemSlotIngredientRenderer renderer;

    public PresenceItemCraftBehaviour(Predicate<EntityRef> matcher, int count) {
        this.matcher = matcher;
        this.count = count;
    }

    @Override
    public boolean isValidAnyAmount(EntityRef ingredient) {
        return matcher.apply(ingredient);
    }

    @Override
    public boolean isValidToCraft(EntityRef entity, int slot, int multiplier) {
        EntityRef ingredient = InventoryUtils.getItemAt(entity, slot);
        if (!matcher.apply(ingredient)) {
            return false;
        }
        ItemComponent itemComponent = ingredient.getComponent(ItemComponent.class);
        return itemComponent != null && itemComponent.stackCount >= count;
    }

    @Override
    public int getMaxMultiplier(EntityRef entity, int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, int slot) {
        if (renderer == null) {
            renderer = new ItemSlotIngredientRenderer();
        }
        renderer.update(entity, slot, new FixedFunction(count));
        return renderer;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, int slot, int multiplier) {
    }
}
