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

import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIInventoryGrid;
import org.terasology.workstation.event.UserUpgradeStationRequest;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;
import org.terasology.workstation.system.recipe.UpgradeRecipe;

import javax.vecmath.Vector2f;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UICraftOnStationInterior extends UIDisplayContainer {
    private UIImage backgroundImage;

    private UIInventoryGrid upgradeGrid;
    private UIInventoryGrid toolsGrid;
    private UIInventoryGrid componentsGrid;
    private UIInventoryGrid outputGrid;

    private UIButton upgradeButton;

    private EntityRef station;
    private String stationType;
    private int upgradeSlots;
    private int toolSlots;
    private int componentSlots;
    private UpgradeCompleteCallback upgradeCompleteCallback;

    private UIAvailableStationRecipesDisplay availableRecipes;

    private String upgradeRecipeDisplayed;

    private int windowWidth = 500;
    private int windowHeight = 320;

    public UICraftOnStationInterior(EntityRef entity, String stationType, String textureUri, Vector2f textureOrigin,
                                    int upgradeSlots, int toolSlots, int componentSlots, UpgradeCompleteCallback upgradeCompleteCallback) {
        this.station = entity;
        this.stationType = stationType;

        this.upgradeSlots = upgradeSlots;
        this.toolSlots = toolSlots;
        this.componentSlots = componentSlots;
        this.upgradeCompleteCallback = upgradeCompleteCallback;

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


        CraftingStationRecipeRegistry craftingRegistry = CoreRegistry.get(CraftingStationRecipeRegistry.class);
        UIAvailableStationRecipesDisplay recipesDisplay =
                new UIAvailableStationRecipesDisplay(new Vector2f(windowWidth - 150, windowHeight - 50), craftingRegistry, stationType, station,
                        upgradeSlots + toolSlots, componentSlots, upgradeSlots, toolSlots);
        recipesDisplay.setPosition(new Vector2f(150, 0));

        addDisplayElement(recipesDisplay);
        addDisplayElement(backgroundImage);
        addDisplayElement(upgradeGrid);
        addDisplayElement(toolsGrid);
        addDisplayElement(componentsGrid);
        addDisplayElement(outputGrid);

        Vector2i displaySize = getDisplaySize();
        setSize(new Vector2f(windowWidth, windowHeight));
        setPosition(new Vector2f((displaySize.x - windowWidth) / 2, (displaySize.y - windowHeight) / 2));
    }

    public void update() {
        super.update();

        CraftingStationRecipeRegistry craftingRegistry = CoreRegistry.get(CraftingStationRecipeRegistry.class);

        String matchingUpgradeRecipe = getMatchingUpgradeRecipe(craftingRegistry);
        if (!isSame(matchingUpgradeRecipe, upgradeRecipeDisplayed)) {
            if (upgradeRecipeDisplayed != null) {
                removeDisplayElement(upgradeButton);
            }
            if (matchingUpgradeRecipe != null) {
                addUpgradeButton(matchingUpgradeRecipe);
            }
            upgradeRecipeDisplayed = matchingUpgradeRecipe;
        }
    }

    private void addUpgradeButton(final String matchingUpgradeRecipe) {
        upgradeRecipeDisplayed = matchingUpgradeRecipe;

        upgradeButton = new UIButton(new Vector2f(80, 48), UIButton.ButtonType.NORMAL);
        upgradeButton.getLabel().setText("Upgrade");
        upgradeButton.setPosition(new Vector2f(50, windowHeight - 48));

        upgradeButton.addClickListener(
                new ClickListener() {
                    @Override
                    public void click(UIDisplayElement element, int button) {
                        CraftingStationRecipeRegistry craftingRegistry = CoreRegistry.get(CraftingStationRecipeRegistry.class);
                        final UpgradeRecipe upgradeRecipe = craftingRegistry.getUpgradeRecipes(stationType).get(matchingUpgradeRecipe);
                        final UpgradeRecipe.UpgradeResult result = upgradeRecipe.getMatchingUpgradeResult(station, 0, upgradeSlots);
                        if (result != null) {
                            EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                            station.send(new UserUpgradeStationRequest(character, stationType, matchingUpgradeRecipe));
                            upgradeCompleteCallback.upgradeComplete();
                        }
                    }
                });

        addDisplayElement(upgradeButton);
    }

    private boolean isSame(String recipe1, String recipe2) {
        if (recipe1 == null && recipe2 == null) {
            return true;
        }
        if (recipe1 == null || recipe2 == null) {
            return false;
        }
        return recipe1.equals(recipe2);
    }

    private String getMatchingUpgradeRecipe(CraftingStationRecipeRegistry craftingRegistry) {
        final Map<String, UpgradeRecipe> upgradeRecipes = craftingRegistry.getUpgradeRecipes(stationType);
        for (Map.Entry<String, UpgradeRecipe> upgradeRecipe : upgradeRecipes.entrySet()) {
            final UpgradeRecipe.UpgradeResult result = upgradeRecipe.getValue().getMatchingUpgradeResult(station, 0, upgradeSlots);
            if (result != null) {
                return upgradeRecipe.getKey();
            }
        }
        return null;
    }
}
