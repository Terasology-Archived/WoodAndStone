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
package org.terasology.workstation.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.workstation.component.CraftingStationComponent;
import org.terasology.workstation.event.OpenCraftingWorkstationRequest;
import org.terasology.workstation.event.UserCraftOnStationRequest;
import org.terasology.workstation.system.recipe.CraftingStationRecipe;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CraftingWorkstationAuthoritySystem implements ComponentSystem {
    @In
    private CraftingStationRecipeRegistry recipeRegistry;

    private PickupBuilder pickupBuilder;

    @Override
    public void initialise() {
        pickupBuilder = new PickupBuilder();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CraftingStationComponent.class})
    public void userActivatesWorkstation(ActivateEvent event, EntityRef entity) {
        entity.send(new OpenCraftingWorkstationRequest());
    }


    @ReceiveEvent
    public void craftOnWorkstationRequestReceived(UserCraftOnStationRequest event, EntityRef station) {
        String recipeId = event.getRecipeId();
        String resultId = event.getResultId();
        CraftingStationRecipe craftingStationRecipe = recipeRegistry.getRecipesForStation(event.getWorkstationType()).get(recipeId);
        CraftingStationRecipe.CraftingStationResult result = craftingStationRecipe.getResultById(resultId);
        final CraftingStationComponent craftingStation = station.getComponent(CraftingStationComponent.class);
        EntityRef resultEntity = result.craftOne(station,
                craftingStation.upgradeSlots + craftingStation.toolSlots, craftingStation.ingredientSlots,
                craftingStation.upgradeSlots, craftingStation.toolSlots);
        if (resultEntity.exists()) {
            pickupBuilder.createPickupFor(resultEntity, station.getComponent(LocationComponent.class).getWorldPosition(), 200);
        }
    }
}
