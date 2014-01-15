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
package org.terasology.was.ui;

import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIInventoryGrid;
import org.terasology.rendering.gui.windows.UIScreenInventory;
import org.terasology.was.system.CraftingStationRecipeRegistry;

import javax.vecmath.Vector2f;

public class UICraftOnStation extends UIScreenInventory {
    private UIImage backgroundImage;

    private UIInventoryGrid upgradeGrid;
    private UIInventoryGrid toolsGrid;
    private UIInventoryGrid componentsGrid;
    private UIInventoryGrid outputGrid;

    private EntityRef entity;
    private int upgradeSlots;
    private int toolSlots;
    private int componentSlots;

    private UIAvailableRecipesDisplay availableRecipes;

    public void setCraftingStation(EntityRef entity, String textureUri, Vector2f textureOrigin, int upgradeSlots, int toolSlots, int componentSlots) {
        this.entity = entity;

        this.upgradeSlots = upgradeSlots;
        this.toolSlots = toolSlots;
        this.componentSlots = componentSlots;

        backgroundImage = new UIImage(Assets.getTexture(textureUri));
        backgroundImage.setTextureOrigin(textureOrigin);
        backgroundImage.setTextureSize(new Vector2f(400, 300));
        backgroundImage.setHorizontalAlign(EHorizontalAlign.CENTER);
        backgroundImage.setVerticalAlign(EVerticalAlign.CENTER);

        upgradeGrid = new UIInventoryGrid(3);
        upgradeGrid.linkToEntity(entity, 0, upgradeSlots);

        toolsGrid = new UIInventoryGrid(3);
        toolsGrid.linkToEntity(entity, upgradeSlots, toolSlots);

        componentsGrid = new UIInventoryGrid(3);
        componentsGrid.linkToEntity(entity, upgradeSlots + toolSlots, componentSlots);

        outputGrid = new UIInventoryGrid(1);
        outputGrid.linkToEntity(entity, upgradeSlots + toolSlots + componentSlots, 1);

        addDisplayElement(backgroundImage);
        addDisplayElement(upgradeGrid);
        addDisplayElement(toolsGrid);
        addDisplayElement(componentsGrid);
        addDisplayElement(outputGrid);

        layout();
    }

    public void update() {
        super.update();

        if (availableRecipes != null) {
            removeDisplayElement(availableRecipes);
        }
        CraftingStationRecipeRegistry craftingRegistry = CoreRegistry.get(CraftingStationRecipeRegistry.class);

        addDisplayElement(new UIAvailableRecipesDisplay(craftingRegistry, entity, upgradeSlots + toolSlots, componentSlots, upgradeSlots, toolSlots));
    }
}
