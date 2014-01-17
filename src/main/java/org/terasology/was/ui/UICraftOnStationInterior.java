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
import org.terasology.math.Vector2i;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIInventoryGrid;
import org.terasology.was.system.CraftingStationRecipeRegistry;

import javax.vecmath.Vector2f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UICraftOnStationInterior extends UIDisplayContainer {
    private UIImage backgroundImage;

    private UIInventoryGrid upgradeGrid;
    private UIInventoryGrid toolsGrid;
    private UIInventoryGrid componentsGrid;
    private UIInventoryGrid outputGrid;

    private EntityRef entity;
    private String stationType;
    private int upgradeSlots;
    private int toolSlots;
    private int componentSlots;

    private UIAvailableStationRecipesDisplay availableRecipes;

    public void setCraftingStation(EntityRef entity, String stationType, String textureUri, Vector2f textureOrigin, int upgradeSlots, int toolSlots, int componentSlots) {
        this.entity = entity;
        this.stationType = stationType;

        this.upgradeSlots = upgradeSlots;
        this.toolSlots = toolSlots;
        this.componentSlots = componentSlots;

        int windowWidth = 500;
        int windowHeight = 320;

        backgroundImage = new UIImage(Assets.getTexture(textureUri));
        backgroundImage.setTextureOrigin(textureOrigin);
        backgroundImage.setTextureSize(new Vector2f(400, 300));
        backgroundImage.setHorizontalAlign(UIDisplayElement.EHorizontalAlign.CENTER);
        backgroundImage.setVerticalAlign(UIDisplayElement.EVerticalAlign.CENTER);

        upgradeGrid = new UIInventoryGrid(3);
        upgradeGrid.linkToEntity(entity, 0, upgradeSlots);
        upgradeGrid.setPosition(new Vector2f(0, windowHeight - 48));

        toolsGrid = new UIInventoryGrid(3);
        toolsGrid.linkToEntity(entity, upgradeSlots, toolSlots);
        toolsGrid.setPosition(new Vector2f(150, windowHeight - 48));

        componentsGrid = new UIInventoryGrid(3);
        componentsGrid.linkToEntity(entity, upgradeSlots + toolSlots, componentSlots);
        componentsGrid.setPosition(new Vector2f(0, 0));

        outputGrid = new UIInventoryGrid(1);
        outputGrid.linkToEntity(entity, upgradeSlots + toolSlots + componentSlots, 1);
        outputGrid.setPosition(new Vector2f(windowWidth - 48, windowHeight - 48));

        addDisplayElement(backgroundImage);
        addDisplayElement(upgradeGrid);
        addDisplayElement(toolsGrid);
        addDisplayElement(componentsGrid);
        addDisplayElement(outputGrid);

        Vector2i displaySize = getDisplaySize();
        setSize(new Vector2f(windowWidth, windowHeight));
        setPosition(new Vector2f((displaySize.x - windowWidth) / 2, (displaySize.y - windowHeight) / 2));

        layout();
    }

    public void update() {
        super.update();

        if (availableRecipes != null) {
            removeDisplayElement(availableRecipes);
        }
        CraftingStationRecipeRegistry craftingRegistry = CoreRegistry.get(CraftingStationRecipeRegistry.class);

        addDisplayElement(new UIAvailableStationRecipesDisplay(craftingRegistry, stationType, entity, upgradeSlots + toolSlots, componentSlots, upgradeSlots, toolSlots));
    }
}
