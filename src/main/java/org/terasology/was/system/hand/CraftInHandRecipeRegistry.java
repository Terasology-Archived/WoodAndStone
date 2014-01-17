package org.terasology.was.system.hand;

import org.terasology.was.system.hand.recipe.CraftInHandRecipe;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftInHandRecipeRegistry {
    public void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe);

    public Map<String, CraftInHandRecipe> getRecipes();
}
