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
import org.terasology.crafting.system.recipe.workstation.SimpleUpgradeRecipe;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationProcessFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftingWorkstationUpgradeProcessFactory implements WorkstationProcessFactory {
    @Override
    public WorkstationProcess createProcess(Prefab prefab) {
        CraftingStationUpgradeRecipeComponent upgrade = prefab.getComponent(CraftingStationUpgradeRecipeComponent.class);
        SimpleUpgradeRecipe upgradeRecipe = new SimpleUpgradeRecipe(upgrade.targetStationType, upgrade.targetStationPrefab, upgrade.resultBlockUri);
        for (String recipeComponent : upgrade.recipeComponents) {
            int starIndex = recipeComponent.indexOf("*");
            if (starIndex > 0) {
                upgradeRecipe.addIngredient(recipeComponent.substring(starIndex + 1), Integer.parseInt(recipeComponent.substring(0, starIndex)));
            } else {
                upgradeRecipe.addIngredient(recipeComponent, 1);
            }
        }

        return new CraftingWorkstationUpgradeProcess(upgrade.stationType, upgrade.targetStationType, upgradeRecipe);
    }
}
