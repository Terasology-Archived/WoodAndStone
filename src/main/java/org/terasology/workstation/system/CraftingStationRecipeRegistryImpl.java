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

import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.workstation.component.CraftingStationRecipeComponent;
import org.terasology.workstation.system.recipe.CraftingStationRecipe;
import org.terasology.workstation.system.recipe.SimpleWorkstationRecipe;
import org.terasology.workstation.system.recipe.UpgradeRecipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = CraftingStationRecipeRegistry.class)
public class CraftingStationRecipeRegistryImpl implements ComponentSystem, CraftingStationRecipeRegistry {
    private Map<String, Map<String, CraftingStationRecipe>> stationRecipes = new HashMap<>();
    private Map<String, Map<String, UpgradeRecipe>> upgradeRecipes = new HashMap<>();
    private Map<String, List<String>> upgradeReverse = new HashMap<>();

    @In
    private PrefabManager prefabManager;

    @Override
    public void initialise() {
        registerPrefabRecipes();
    }

    @Override
    public void shutdown() {
    }

    private void registerPrefabRecipes() {
        for (Prefab prefab : prefabManager.listPrefabs(CraftingStationRecipeComponent.class)) {
            CraftingStationRecipeComponent recipe = prefab.getComponent(CraftingStationRecipeComponent.class);

            SimpleWorkstationRecipe workstationRecipe = new SimpleWorkstationRecipe();
            for (String recipeComponent : recipe.recipeComponents) {
                String[] split = recipeComponent.split("\\*", 2);
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                workstationRecipe.addIngredient(type, count);
            }
            for (String recipeTool : recipe.recipeTools) {
                String[] split = recipeTool.split("\\*", 2);
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                workstationRecipe.addRequiredTool(type, count);
            }

            if (recipe.blockResult != null) {
                String[] split = recipe.blockResult.split("\\*", 2);
                int count = Integer.parseInt(split[0]);
                String block = split[1];
                workstationRecipe.setBlockResult(block, (byte) count);
            }
            if (recipe.itemResult != null) {
                String[] split = recipe.itemResult.split("\\*", 2);
                int count = Integer.parseInt(split[0]);
                String item = split[1];
                workstationRecipe.setItemResult(item, (byte) count);
            }

            addCraftingStationRecipe(recipe.stationType, recipe.recipeId, workstationRecipe);
        }
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
    public void addStationUpgradeRecipe(String stationType, String newStationType, String recipeId, UpgradeRecipe recipe) {
        Map<String, UpgradeRecipe> stationTypeUpgrades = upgradeRecipes.get(stationType);
        if (stationTypeUpgrades == null) {
            stationTypeUpgrades = new LinkedHashMap<>();
            upgradeRecipes.put(stationType, stationTypeUpgrades);
        }
        stationTypeUpgrades.put(recipeId, recipe);

        List<String> downgraded = upgradeReverse.get(newStationType);
        if (downgraded == null) {
            downgraded = new LinkedList<>();
            upgradeReverse.put(newStationType, downgraded);
        }
        downgraded.add(stationType);
    }

    @Override
    public Map<String, CraftingStationRecipe> getCraftingRecipes(String stationType) {
        Map<String, CraftingStationRecipe> result = new LinkedHashMap<>();
        appendCraftingRecipes(result, stationType);
        return result;
    }

    private void appendCraftingRecipes(Map<String, CraftingStationRecipe> result, String stationType) {
        List<String> basicWorkstations = upgradeReverse.get(stationType);
        if (basicWorkstations != null) {
            for (String basicWorkstation : basicWorkstations) {
                appendCraftingRecipes(result, basicWorkstation);
            }
        }
        Map<String, CraftingStationRecipe> recipesForThisWorkstation = stationRecipes.get(stationType);
        if (recipesForThisWorkstation != null) {
            result.putAll(recipesForThisWorkstation);
        }
    }

    public Map<String, UpgradeRecipe> getUpgradeRecipes(String stationType) {
        Map<String, UpgradeRecipe> recipeMap = upgradeRecipes.get(stationType);
        if (recipeMap == null) {
            return Collections.emptyMap();
        }
        return recipeMap;
    }
}
