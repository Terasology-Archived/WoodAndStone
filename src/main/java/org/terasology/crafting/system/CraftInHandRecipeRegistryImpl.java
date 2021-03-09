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
    private Map<String, CraftInHandRecipe> recipes = new LinkedHashMap<>();
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
