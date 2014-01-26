package org.terasology.multiBlock;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface MultiBlockFormRecipeRegistry {
    void addMultiBlockFormItemRecipe(MultiBlockFormItemRecipe recipe);

    Collection<MultiBlockFormItemRecipe> getMultiBlockFormItemRecipes();
}
