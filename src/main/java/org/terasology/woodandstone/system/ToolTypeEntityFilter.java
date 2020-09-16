// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.system;

import com.google.common.base.Predicate;
import org.terasology.crafting.component.CraftingStationToolComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ToolTypeEntityFilter implements Predicate<EntityRef> {
    private final String toolType;

    public ToolTypeEntityFilter(String toolType) {
        this.toolType = toolType;
    }

    @Override
    public boolean apply(EntityRef item) {
        CraftingStationToolComponent tool = item.getComponent(CraftingStationToolComponent.class);
        return tool != null && tool.type.contains(toolType);
    }
}
