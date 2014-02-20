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
package org.terasology.crafting.system.recipe.behaviour;

import com.google.common.base.Function;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface IngredientCraftBehaviour {
    boolean isValidAnyAmount(EntityRef item);

    boolean isValid(EntityRef ingredient, int multiplier);

    int getMaxMultiplier(EntityRef ingredient);

    Function<Integer, Integer> getCountBasedOnMultiplier();

    void processIngredient(EntityRef instigator, EntityRef entity, EntityRef ingredient, int multiplier);
}
