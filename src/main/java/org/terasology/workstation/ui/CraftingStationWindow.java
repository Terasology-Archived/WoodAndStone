/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.workstation.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.workstation.event.UserUpgradeStationRequest;
import org.terasology.workstation.system.CraftingStationRecipeRegistry;
import org.terasology.workstation.system.recipe.UpgradeRecipe;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationWindow extends CoreScreenLayer {
    private InventoryGrid ingredients;
    private InventoryGrid upgrades;
    private UIButton upgradeButton;
    private InventoryGrid tools;
    private StationAvailableRecipesWidget stationRecipes;
    private InventoryGrid result;
    private InventoryGrid player;
    private UIImage stationBackground;

    private EntityRef station;
    private String stationType;
    private int upgradeSlots;

    private String upgradeRecipeDisplayed;
    private String matchingUpgradeRecipe;

    @Override
    public void initialise() {
        ingredients = find("ingredientsInventory", InventoryGrid.class);
        upgrades = find("upgradesInventory", InventoryGrid.class);
        upgradeButton = find("upgradeButton", UIButton.class);
        tools = find("toolsInventory", InventoryGrid.class);
        stationRecipes = find("availableRecipes", StationAvailableRecipesWidget.class);
        result = find("resultInventory", InventoryGrid.class);
        player = find("playerInventory", InventoryGrid.class);
        stationBackground = find("stationBackground", UIImage.class);

        upgradeButton.setText("Upgrade");
    }

    public void setCraftingStation(final EntityRef station, final String stationType, Texture texture, int upgradeSlots, int toolSlots, int componentSlots) {
        this.station = station;
        this.stationType = stationType;
        this.upgradeSlots = upgradeSlots;

        ingredients.setTargetEntity(station);
        ingredients.setCellOffset(upgradeSlots + toolSlots);
        ingredients.setMaxCellCount(componentSlots);

        upgrades.setTargetEntity(station);
        upgrades.setCellOffset(0);
        upgrades.setMaxCellCount(upgradeSlots);

        tools.setTargetEntity(station);
        tools.setCellOffset(upgradeSlots);
        tools.setMaxCellCount(toolSlots);

        stationRecipes.setStation(station);
        stationRecipes.setStationType(stationType);
        stationRecipes.setComponentFromSlot(upgradeSlots + toolSlots);
        stationRecipes.setComponentSlotCount(componentSlots);
        stationRecipes.setToolFromSlot(upgradeSlots);
        stationRecipes.setToolSlotCount(toolSlots);

        result.setTargetEntity(station);
        result.setCellOffset(upgradeSlots + toolSlots + componentSlots);
        result.setMaxCellCount(1);

        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);

        upgradeButton.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                        station.send(new UserUpgradeStationRequest(character, stationType, matchingUpgradeRecipe));
                    }
                });
        upgradeButton.setVisible(false);

        stationBackground.setImage(texture);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        CraftingStationRecipeRegistry craftingRegistry = CoreRegistry.get(CraftingStationRecipeRegistry.class);

        matchingUpgradeRecipe = getMatchingUpgradeRecipe(craftingRegistry);
        if (!isSame(matchingUpgradeRecipe, upgradeRecipeDisplayed)) {
            if (upgradeRecipeDisplayed != null) {
                upgradeButton.setVisible(false);
            }
            if (matchingUpgradeRecipe != null) {
                upgradeButton.setVisible(true);
            }
            upgradeRecipeDisplayed = matchingUpgradeRecipe;
        }

        if (!station.exists()) {
            CoreRegistry.get(NUIManager.class).closeScreen(this);
        }
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

    @Override
    public boolean isModal() {
        return false;
    }
}
