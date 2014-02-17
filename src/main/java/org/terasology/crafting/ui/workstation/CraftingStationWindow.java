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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.workstation.ui.WorkstationUI;

import java.util.Collections;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingStationWindow extends CoreScreenLayer implements WorkstationUI {
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
    public void initializeWorkstation(final EntityRef station) {
        CraftingStationComponent craftingStation = station.getComponent(CraftingStationComponent.class);

        this.station = station;

        WorkstationInventoryComponent workstationInventory = station.getComponent(WorkstationInventoryComponent.class);
        WorkstationInventoryComponent.SlotAssignment upgradeAssignments = workstationInventory.slotAssignments.get("UPGRADE");
        WorkstationInventoryComponent.SlotAssignment inputAssignments = workstationInventory.slotAssignments.get("INPUT");
        WorkstationInventoryComponent.SlotAssignment toolAssignments = workstationInventory.slotAssignments.get("TOOL");
        WorkstationInventoryComponent.SlotAssignment resultAssignments = workstationInventory.slotAssignments.get("RESULT");

        ingredients.setTargetEntity(station);
        ingredients.setCellOffset(inputAssignments.slotStart);
        ingredients.setMaxCellCount(inputAssignments.slotCount);

        upgrades.setTargetEntity(station);
        upgrades.setCellOffset(upgradeAssignments.slotStart);
        upgrades.setMaxCellCount(upgradeAssignments.slotCount);

        tools.setTargetEntity(station);
        tools.setCellOffset(toolAssignments.slotStart);
        tools.setMaxCellCount(toolAssignments.slotCount);

        stationRecipes.setStation(station);

        result.setTargetEntity(station);
        result.setCellOffset(resultAssignments.slotStart);
        result.setMaxCellCount(resultAssignments.slotCount);

        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);

        upgradeButton.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                        station.send(new WorkstationProcessRequest(character, matchingUpgradeRecipe, null));
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
                final UpgradeRecipe.UpgradeResult result = upgradeRecipe.getMatchingUpgradeResult(station);
                if (result != null) {
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
