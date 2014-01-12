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
package org.terasology.was.system;

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
import org.terasology.was.component.CraftInHandRecipeComponent;
import org.terasology.was.component.PlantFibreComponent;
import org.terasology.was.event.UserCraftInHandRequest;
import org.terasology.was.system.recipe.SimpleCraftInHandRecipe;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CraftToolsInHandAuthoritySystem implements ComponentSystem {
    @In
    private SlotBasedInventoryManager inventoryManager;
    @In
    private EntityManager entityManager;
    private PickupBuilder pickupBuilder;

    private List<CraftInHandRecipe> recipes = new LinkedList<>();

    @Override
    public void initialise() {
        recipes.add(
                new SimpleCraftInHandRecipe("stick", "stone", "WoodAndStone:hammer"));
        pickupBuilder = new PickupBuilder();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void craftInHandRequestReceived(UserCraftInHandRequest event, EntityRef character) {
        EntityRef handleItem = event.getHandleItem();
        EntityRef toolHeadItem = event.getToolHeadItem();

        CraftInHandRecipeComponent handle = handleItem.getComponent(CraftInHandRecipeComponent.class);
        CraftInHandRecipeComponent toolHead = toolHeadItem.getComponent(CraftInHandRecipeComponent.class);

        String resultPrefab = findResultForMatchingRecipe(handle, toolHead);

        if (resultPrefab != null) {
            EntityRef resultEntity = entityManager.create(resultPrefab);

            int slotWithHandle = inventoryManager.findSlotWithItem(character, handleItem);
            int slotWithToolHead = inventoryManager.findSlotWithItem(character, toolHeadItem);
            int slotWithPlantFibre = findSlotWithPlantFibre(character);

            if (slotWithHandle != -1 && slotWithToolHead != -1 && slotWithPlantFibre != -1) {
                inventoryManager.removeItem(character, handleItem, 1);
                inventoryManager.removeItem(character, toolHeadItem, 1);
                inventoryManager.removeItem(character, inventoryManager.getItemInSlot(character, slotWithPlantFibre));

                pickupBuilder.createPickupFor(resultEntity, character.getComponent(LocationComponent.class).getWorldPosition(), 200);
            }
        }
    }

    private int findSlotWithPlantFibre(EntityRef character) {
        int slotCount = inventoryManager.getNumSlots(character);
        for (int i = 0; i < slotCount; i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(character, i);
            if (itemInSlot.hasComponent(PlantFibreComponent.class))
                return i;
        }
        return -1;
    }

    private String findResultForMatchingRecipe(CraftInHandRecipeComponent handle, CraftInHandRecipeComponent toolHead) {
        if (handle == null || toolHead == null)
            return null;
        for (CraftInHandRecipe recipe : recipes) {
            if (recipe.matchesRecipe(handle, toolHead)) {
                return recipe.getResultPrefab();
            }
        }
        return null;
    }

    public interface CraftInHandRecipe {
        public boolean matchesRecipe(CraftInHandRecipeComponent handle, CraftInHandRecipeComponent toolHead);

        public String getResultPrefab();
    }
}
