package org.terasology.was.system;

import org.terasology.was.system.recipe.station.CraftingStationRecipe;
import org.terasology.was.system.recipe.station.UpgradeRecipe;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftingStationRecipeRegistry {

    public void addCraftingStationRecipe(String stationType, String recipeId, CraftingStationRecipe recipe);

    public void addStationUpgradeRecipe(String stationType, String recipeId, UpgradeRecipe recipe);
}
