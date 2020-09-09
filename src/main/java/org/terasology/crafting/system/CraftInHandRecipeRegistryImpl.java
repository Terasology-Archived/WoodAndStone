// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system;

import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = CraftInHandRecipeRegistry.class)
public class CraftInHandRecipeRegistryImpl extends BaseComponentSystem implements CraftInHandRecipeRegistry {
    private final Map<String, CraftInHandRecipe> recipes = new LinkedHashMap<>();
    private boolean disabled;

    @Override
    public void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe) {
        recipes.put(recipeId, craftInHandRecipe);
    }

    @Override
    public Map<String, CraftInHandRecipe> getRecipes() {
        return recipes;
    }

    @Override
    public void disableCraftingInHand() {
        disabled = true;
    }

    @Override
    public boolean isCraftingInHandDisabled() {
        return disabled;
    }
}
