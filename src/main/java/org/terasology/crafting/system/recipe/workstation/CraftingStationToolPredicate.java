// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.workstation;

import com.google.common.base.Predicate;
import org.terasology.crafting.component.CraftingStationToolComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationToolPredicate implements Predicate<EntityRef> {
    private final String toolType;

    public CraftingStationToolPredicate(String toolType) {
        this.toolType = toolType;
    }

    @Override
    public boolean apply(EntityRef input) {
        CraftingStationToolComponent component = input.getComponent(CraftingStationToolComponent.class);
        return component != null && component.type.contains(toolType);
    }
}
