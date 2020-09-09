// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.hand;

import com.google.common.base.Predicate;
import org.terasology.crafting.component.CraftInHandIngredientComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftInHandIngredientPredicate implements Predicate<EntityRef> {
    private final String itemType;

    public CraftInHandIngredientPredicate(String itemType) {
        this.itemType = itemType;
    }

    @Override
    public boolean apply(EntityRef input) {
        CraftInHandIngredientComponent craftComponent = input.getComponent(CraftInHandIngredientComponent.class);
        return craftComponent != null && craftComponent.componentType.equals(itemType);
    }
}
