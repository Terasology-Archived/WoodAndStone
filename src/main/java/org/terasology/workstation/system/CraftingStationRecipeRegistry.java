package org.terasology.workstation.system;

import org.terasology.workstation.system.recipe.CraftingStationRecipe;
import org.terasology.workstation.system.recipe.UpgradeRecipe;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftingStationRecipeRegistry {

    public void addCraftingStationRecipe(String stationType, String recipeId, CraftingStationRecipe recipe);

    public void addStationUpgradeRecipe(String stationType, String recipeId, UpgradeRecipe recipe);

    public Map<String, CraftingStationRecipe> getCraftingRecipes(String stationType);

    public Map<String, UpgradeRecipe> getUpgradeRecipes(String stationType);
}
