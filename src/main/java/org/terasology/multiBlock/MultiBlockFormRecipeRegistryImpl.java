package org.terasology.multiBlock;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
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
