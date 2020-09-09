// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import com.google.common.base.Predicate;
import org.terasology.crafting.component.CraftingStationIngredientComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.herbalism.component.HerbComponent;

public class HerbalismStationIngredientPredicate implements Predicate<EntityRef> {
    private final String itemType;

    public HerbalismStationIngredientPredicate(String itemType) {
        this.itemType = itemType;
    }

    @Override
    public boolean apply(EntityRef input) {
        HerbComponent hComponent = input.getComponent(HerbComponent.class);
        CraftingStationIngredientComponent component = input.getComponent(CraftingStationIngredientComponent.class);

        // If this contains a valid instance of HerbComponent, CraftingStationIngredientComponent and the input's
        // type matches the itemType of this predicate.
        return hComponent != null && component != null && component.type.equalsIgnoreCase(itemType);
    }
}
