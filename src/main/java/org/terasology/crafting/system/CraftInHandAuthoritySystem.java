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
package org.terasology.crafting.system;

import org.terasology.crafting.event.UserCraftInHandRequest;
import org.terasology.crafting.system.recipe.CraftInHandRecipe;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.location.LocationComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CraftInHandAuthoritySystem implements ComponentSystem {
    @In
    private SlotBasedInventoryManager inventoryManager;
    @In
    private EntityManager entityManager;
    @In
    private CraftInHandRecipeRegistry recipeRegistry;

    private PickupBuilder pickupBuilder;

    @Override
    public void initialise() {
        pickupBuilder = new PickupBuilder();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void craftInHandRequestReceived(UserCraftInHandRequest event, EntityRef character) {
        if (!recipeRegistry.isCraftingInHandDisabled()) {
            String recipeId = event.getRecipeId();
            String resultId = event.getResultId();
            CraftInHandRecipe craftInHandRecipe = recipeRegistry.getRecipes().get(recipeId);
            CraftInHandRecipe.CraftInHandResult result = craftInHandRecipe.getResultById(resultId);
            EntityRef resultEntity = result.craftOne(character);
            if (resultEntity.exists()) {
                pickupBuilder.createPickupFor(resultEntity, character.getComponent(LocationComponent.class).getWorldPosition(), 200);
            }
        }
    }
}
