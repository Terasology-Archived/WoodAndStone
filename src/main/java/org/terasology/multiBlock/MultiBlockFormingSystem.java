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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.multiBlock.recipe.MultiBlockFormItemRecipe;
import org.terasology.registry.In;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MultiBlockFormingSystem extends BaseComponentSystem {
    @In
    private MultiBlockFormRecipeRegistry recipeRegistry;

    @ReceiveEvent(components = {ItemComponent.class})
    public void formMultiBlockWithItem(ActivateEvent event, EntityRef item) {
        for (MultiBlockFormItemRecipe multiBlockFormItemRecipe : recipeRegistry.getMultiBlockFormItemRecipes()) {
            if (multiBlockFormItemRecipe.isActivator(item)) {
                if (multiBlockFormItemRecipe.processActivation(event)) {
                    break;
                }
            }
        }
    }
}
