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
package org.terasology.crafting.system;

import org.terasology.crafting.component.CraftingStationUpgradeRecipeComponent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.workstation.system.WorkstationRegistry;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class CraftingStationCommonSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry recipeRegistry;

    @Override
    public void initialise() {
        recipeRegistry.registerProcessFactory(CraftingStationUpgradeRecipeComponent.PROCESS_TYPE, new CraftingWorkstationUpgradeProcessFactory());
    }
//
//    @ReceiveEvent(components = {CraftingStationComponent.class})
//    public void craftStationGetsItem(BeforeItemPutInInventory event, EntityRef craftingStation) {
//        WorkstationComponent workstation = craftingStation.getComponent(WorkstationComponent.class);
//        List<CraftingStationRecipe> recipes = new LinkedList<>();
//        List<UpgradeRecipe> upgradeRecipes = new LinkedList<>();
//
//        for (WorkstationProcess workstationProcess : recipeRegistry.getWorkstationProcesses(workstation.supportedProcessTypes)) {
//            if (workstationProcess instanceof CraftingWorkstationProcess) {
//                recipes.add(((CraftingWorkstationProcess) workstationProcess).getCraftingWorkstationRecipe());
//            }
//        }
//
//        for (WorkstationProcess workstationProcess : recipeRegistry.getWorkstationProcesses(Collections.singleton(CraftingStationUpgradeRecipeComponent.PROCESS_TYPE))) {
//            if (workstationProcess instanceof CraftingWorkstationUpgradeProcess) {
//                upgradeRecipes.add(((CraftingWorkstationUpgradeProcess) workstationProcess).getUpgradeRecipe());
//            }
//        }
//
//        CraftingStationComponent craftingStationComponent = craftingStation.getComponent(CraftingStationComponent.class);
//        int slot = event.getSlot();
//
//        if (isIngredientSlot(craftingStationComponent, slot)) {
//            // Only valid ingredients can be put into ingredients slot
//            for (CraftingStationRecipe craftingStationRecipe : recipes) {
//                if (craftingStationRecipe.hasAsComponent(event.getItem())) {
//                    return;
//                }
//            }
//
//            event.consume();
//        } else if (isToolSlot(craftingStationComponent, slot)) {
//            // Only valid tools can be put into tools slot
//            for (CraftingStationRecipe craftingStationRecipe : recipes) {
//                if (craftingStationRecipe.hasAsTool(event.getItem())) {
//                    return;
//                }
//            }
//
//            event.consume();
//        } else if (isUpgradeSlot(craftingStationComponent, slot)) {
//            // Only valid upgrade components can be put into upgrades slot
//            for (UpgradeRecipe upgradeRecipe : upgradeRecipes) {
//                if (upgradeRecipe.isUpgradeComponent(event.getItem())) {
//                    return;
//                }
//            }
//
//            event.consume();
//        } else if (isResultSlot(craftingStationComponent, slot)) {
//            if (event.getInstigator() != craftingStation) {
//                // Nothing else can be put into result slot
//                event.consume();
//            }
//        }
//    }
//
//    private boolean isUpgradeSlot(CraftingStationComponent craftingStationComponent, int slot) {
//        return slot >= 0 && slot < craftingStationComponent.upgradeSlots;
//    }
//
//    private boolean isIngredientSlot(CraftingStationComponent craftingStationComponent, int slot) {
//        return slot >= craftingStationComponent.upgradeSlots + craftingStationComponent.toolSlots
//                && slot < craftingStationComponent.upgradeSlots + craftingStationComponent.toolSlots + craftingStationComponent.ingredientSlots;
//    }
//
//    private boolean isToolSlot(CraftingStationComponent craftingStationComponent, int slot) {
//        return slot >= craftingStationComponent.upgradeSlots
//                && slot < craftingStationComponent.upgradeSlots + craftingStationComponent.toolSlots;
//    }
//
//    private boolean isResultSlot(CraftingStationComponent craftingStationComponent, int slot) {
//        return slot >= craftingStationComponent.upgradeSlots + craftingStationComponent.toolSlots + craftingStationComponent.ingredientSlots;
//    }
}
