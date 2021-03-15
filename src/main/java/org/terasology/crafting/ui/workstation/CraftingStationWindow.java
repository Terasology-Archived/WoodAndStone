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
package org.terasology.crafting.ui.workstation;

import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.crafting.component.CraftingStationUpgradeRecipeComponent;
import org.terasology.crafting.system.CraftingWorkstationUpgradeProcess;
import org.terasology.crafting.system.recipe.workstation.UpgradeRecipe;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.ActivateEventListener;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIImage;
import org.terasology.was.ui.WorkstationScreenUtils;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;

import java.util.Collections;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationWindow extends BaseInteractionScreen {
    private InventoryGrid ingredients;
    private InventoryGrid upgrades;
    private UIButton upgradeButton;
    private InventoryGrid tools;
    private StationAvailableRecipesWidget stationRecipes;
    private InventoryGrid result;
    private InventoryGrid player;
    private UIImage stationBackground;

    private EntityRef station;

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

    @Override
    protected void initializeWithInteractionTarget(final EntityRef workstation) {
        CraftingStationComponent craftingStation = workstation.getComponent(CraftingStationComponent.class);

        this.station = workstation;

        WorkstationScreenUtils.setupInventoryGrid(workstation, ingredients, "INPUT");
        WorkstationScreenUtils.setupInventoryGrid(workstation, tools, "TOOL");
        WorkstationScreenUtils.setupInventoryGrid(workstation, upgrades, "UPGRADE");
        WorkstationScreenUtils.setupInventoryGrid(workstation, result, "OUTPUT");

        stationRecipes.setStation(workstation);

        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);

        upgradeButton.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                        character.send(new WorkstationProcessRequest(workstation, matchingUpgradeRecipe));
                    }
                });
        upgradeButton.setVisible(false);

        stationBackground.setImage(craftingStation.workstationUITexture);
    }

    @Override
    public void update(float delta) {
        if (!station.exists()) {
            CoreRegistry.get(NUIManager.class).closeScreen(this);
            return;
        }
        super.update(delta);

        WorkstationRegistry craftingRegistry = CoreRegistry.get(WorkstationRegistry.class);

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

    private String getMatchingUpgradeRecipe(WorkstationRegistry craftingRegistry) {
        for (WorkstationProcess workstationProcess : craftingRegistry.getWorkstationProcesses(Collections.singleton(CraftingStationUpgradeRecipeComponent.PROCESS_TYPE))) {
            if (workstationProcess instanceof CraftingWorkstationUpgradeProcess) {
                UpgradeRecipe upgradeRecipe = ((CraftingWorkstationUpgradeProcess) workstationProcess).getUpgradeRecipe();
                final UpgradeRecipe.UpgradeResult upgradeResult = upgradeRecipe.getMatchingUpgradeResult(station);
                if (upgradeResult != null) {
                    return workstationProcess.getId();
                }
            }
        }
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
