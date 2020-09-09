// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system;

import org.terasology.crafting.component.CraftingStationUpgradeRecipeComponent;
import org.terasology.crafting.system.recipe.workstation.SimpleUpgradeRecipe;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationProcessFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingWorkstationUpgradeProcessFactory implements WorkstationProcessFactory {
    @Override
    public WorkstationProcess createProcess(Prefab prefab) {
        CraftingStationUpgradeRecipeComponent upgrade =
                prefab.getComponent(CraftingStationUpgradeRecipeComponent.class);
        SimpleUpgradeRecipe upgradeRecipe = new SimpleUpgradeRecipe(upgrade.targetStationType,
                upgrade.targetStationPrefab, upgrade.resultBlockUri);
        for (String recipeComponent : upgrade.recipeComponents) {
            int starIndex = recipeComponent.indexOf("*");
            if (starIndex > 0) {
                upgradeRecipe.addIngredient(recipeComponent.substring(starIndex + 1),
                        Integer.parseInt(recipeComponent.substring(0, starIndex)));
            } else {
                upgradeRecipe.addIngredient(recipeComponent, 1);
            }
        }

        return new CraftingWorkstationUpgradeProcess(upgrade.stationType, upgrade.targetStationType, upgradeRecipe);
    }
}
