// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.workstation;

import com.google.common.base.Predicate;
import org.terasology.crafting.component.CraftingStationIngredientComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationIngredientPredicate implements Predicate<EntityRef> {
    private final String itemType;

    public CraftingStationIngredientPredicate(String itemType) {
        this.itemType = itemType;
    }

    @Override
    public boolean apply(EntityRef input) {
        CraftingStationIngredientComponent component = input.getComponent(CraftingStationIngredientComponent.class);
        return component != null && component.type.equals(itemType);
    }
}
