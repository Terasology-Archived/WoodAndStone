// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system;

import org.terasology.crafting.event.UserCraftInHandRequest;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.inventory.logic.InventoryManager;

import java.util.List;

@RegisterSystem(RegisterMode.AUTHORITY)
public class CraftInHandAuthoritySystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private CraftInHandRecipeRegistry recipeRegistry;

    @In
    private InventoryManager inventoryManager;

    @ReceiveEvent
    public void craftInHandRequestReceived(UserCraftInHandRequest event, EntityRef character) {
        if (!recipeRegistry.isCraftingInHandDisabled()) {
            String recipeId = event.getRecipeId();
            final List<String> parameters = event.getParameters();
            CraftInHandRecipe craftInHandRecipe = recipeRegistry.getRecipes().get(recipeId);
            if (craftInHandRecipe != null) {
                CraftInHandRecipe.CraftInHandResult result = craftInHandRecipe.getResultByParameters(parameters);
                if (result != null) {
                    EntityRef resultEntity = result.craft(character, event.getCount());
                    if (resultEntity.exists()) {
                        resultEntity.send(new DropItemEvent(character.getComponent(LocationComponent.class).getWorldPosition()));
                    }
                }
            }
        }
    }
}
