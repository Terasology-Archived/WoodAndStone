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
package org.terasology.workstation.system;

import org.terasology.workstation.system.recipe.CraftingStationRecipe;
import org.terasology.workstation.system.recipe.UpgradeRecipe;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftingStationRecipeRegistry {

    void addCraftingStationRecipe(String stationType, String recipeId, CraftingStationRecipe recipe);

    void addStationUpgradeRecipe(String stationType, String recipeId, UpgradeRecipe recipe);

    Map<String, CraftingStationRecipe> getCraftingRecipes(String stationType);

    Map<String, UpgradeRecipe> getUpgradeRecipes(String stationType);
}
