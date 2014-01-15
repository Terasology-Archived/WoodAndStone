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
package org.terasology.was.system;

import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.Share;
import org.terasology.was.system.recipe.station.CraftingStationRecipe;
import org.terasology.was.system.recipe.station.UpgradeRecipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = CraftingStationRecipeRegistry.class)
public class CraftingStationRecipeRegistryImpl implements ComponentSystem, CraftingStationRecipeRegistry {
    private Map<String, Map<String, CraftingStationRecipe>> stationRecipes = new HashMap<>();
    private Map<String, Map<String, UpgradeRecipe>> upgradeRecipes = new HashMap<>();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void addCraftingStationRecipe(String stationType, String recipeId, CraftingStationRecipe recipe) {
        Map<String, CraftingStationRecipe> stationTypeRecipes = stationRecipes.get(stationType);
        if (stationTypeRecipes == null) {
            stationTypeRecipes = new LinkedHashMap<>();
            stationRecipes.put(stationType, stationTypeRecipes);
        }
        stationTypeRecipes.put(recipeId, recipe);
    }

    @Override
    public void addStationUpgradeRecipe(String stationType, String recipeId, UpgradeRecipe recipe) {
        Map<String, UpgradeRecipe> stationTypeUpgrades = upgradeRecipes.get(stationType);
        if (stationTypeUpgrades == null) {
            stationTypeUpgrades = new LinkedHashMap<>();
            upgradeRecipes.put(stationType, stationTypeUpgrades);
        }
        stationTypeUpgrades.put(recipeId, recipe);
    }

    @Override
    public Map<String, CraftingStationRecipe> getRecipesForStation(String stationType) {
        Map<String, CraftingStationRecipe> recipeMap = stationRecipes.get(stationType);
        if (recipeMap == null)
            return Collections.emptyMap();
        return recipeMap;
    }

    public Map<String, UpgradeRecipe> getRecipesForUpgrade(String stationType) {
        Map<String, UpgradeRecipe> recipeMap = upgradeRecipes.get(stationType);
        if (recipeMap == null)
            return Collections.emptyMap();
        return recipeMap;
    }
}
