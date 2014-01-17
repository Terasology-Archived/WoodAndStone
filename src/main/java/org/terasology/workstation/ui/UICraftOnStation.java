/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.workstation.ui;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.gui.windows.UIScreenInventory;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;

import javax.vecmath.Vector2f;

public class UICraftOnStation extends UIScreenInventory {
    private UICraftOnStationInterior interior;
    private UIAvailableStationRecipesDisplay allRecipesDisplay;
    private EntityRef station;
    private String stationType;
    private int upgradeSlots;
    private int toolSlots;
    private int componentSlots;

    public void setCraftingStation(EntityRef entity, String stationType, String textureUri, Vector2f textureOrigin, int upgradeSlots, int toolSlots, int componentSlots) {
        station = entity;
        this.stationType = stationType;
        this.upgradeSlots = upgradeSlots;
        this.toolSlots = toolSlots;
        this.componentSlots = componentSlots;
        interior = new UICraftOnStationInterior();
        interior.setCraftingStation(entity, stationType, textureUri, textureOrigin, upgradeSlots, toolSlots, componentSlots);
        addDisplayElement(interior);
        updateRecipes();
    }

    public void updateRecipes() {
        if (allRecipesDisplay != null) {
            removeDisplayElement(allRecipesDisplay);
            allRecipesDisplay.dispose();
        }

        CraftingStationRecipeRegistry registry = CoreRegistry.get(CraftingStationRecipeRegistry.class);

        allRecipesDisplay = new UIAvailableStationRecipesDisplay(registry, stationType, station, upgradeSlots + toolSlots, componentSlots, upgradeSlots, toolSlots);
        allRecipesDisplay.setHorizontalAlign(EHorizontalAlign.CENTER);
        allRecipesDisplay.setVerticalAlign(EVerticalAlign.TOP);
        addDisplayElement(allRecipesDisplay);

        layout();
    }
}
