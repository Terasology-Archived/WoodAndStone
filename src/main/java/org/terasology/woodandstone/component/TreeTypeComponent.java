// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.items.AddToBlockBasedItem;
import org.terasology.inventory.logic.ItemDifferentiating;

import java.util.Objects;

@AddToBlockBasedItem
public class TreeTypeComponent implements Component, ItemDifferentiating {
    public String treeType;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TreeTypeComponent that = (TreeTypeComponent) o;

        return treeType != null ? treeType.equals(that.treeType) : that.treeType == null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(treeType);
    }
}
