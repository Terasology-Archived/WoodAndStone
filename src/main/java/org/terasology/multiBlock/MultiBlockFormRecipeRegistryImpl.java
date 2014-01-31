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
package org.terasology.multiBlock;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.multiBlock.recipe.MultiBlockFormItemRecipe;
import org.terasology.registry.Share;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = MultiBlockFormRecipeRegistry.class)
public class MultiBlockFormRecipeRegistryImpl extends BaseComponentSystem implements MultiBlockFormRecipeRegistry {
    private Set<MultiBlockFormItemRecipe> itemRecipes = new HashSet<>();

    @Override
    public void addMultiBlockFormItemRecipe(MultiBlockFormItemRecipe recipe) {
        itemRecipes.add(recipe);
    }

    @Override
    public Collection<MultiBlockFormItemRecipe> getMultiBlockFormItemRecipes() {
        return Collections.unmodifiableCollection(itemRecipes);
    }
}
