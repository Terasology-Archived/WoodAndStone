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
import org.terasology.was.event.UserCraftInHandRequest;
import org.terasology.was.system.recipe.hand.CompositeTypeBasedCraftInHandRecipe;
import org.terasology.was.system.recipe.hand.CraftInHandRecipe;
import org.terasology.was.system.recipe.hand.ItemCraftBehaviour;
import org.terasology.was.system.recipe.hand.SimpleConsumingCraftInHandRecipe;
import org.terasology.was.system.recipe.hand.behaviour.ReduceItemDurabilityCraftBehaviour;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CraftInHandAuthoritySystem implements ComponentSystem {
    @In
    private SlotBasedInventoryManager inventoryManager;
    @In
    private EntityManager entityManager;
    private PickupBuilder pickupBuilder;

    private List<CraftInHandRecipe> recipes = new LinkedList<>();

    @Override
    public void initialise() {
        addCraftInHandRecipe(
                new SimpleConsumingCraftInHandRecipe("stick", "binding", "stone", "WoodAndStone:hammer"));
        addCraftInHandRecipe(
                new SimpleConsumingCraftInHandRecipe("stone", "binding", "stick", "WoodAndStone:hammer"));
        addCraftInHandRecipe(
                new CompositeTypeBasedCraftInHandRecipe("stone", "hammer", null,
                        Collections.<String, ItemCraftBehaviour>singletonMap("hammer", new ReduceItemDurabilityCraftBehaviour(1)),
                        "WoodAndStone:sharpStone"));
        addCraftInHandRecipe(
                new SimpleConsumingCraftInHandRecipe("sharpStone", "binding", "stick", "Core:axe"));
        addCraftInHandRecipe(
                new SimpleConsumingCraftInHandRecipe("stick", "binding", "sharpStone", "Core:axe"));
        pickupBuilder = new PickupBuilder();
    }

    @Override
    public void shutdown() {
    }

    public void addCraftInHandRecipe(CraftInHandRecipe craftInHandRecipe) {
        recipes.add(craftInHandRecipe);
    }

    @ReceiveEvent
    public void craftInHandRequestReceived(UserCraftInHandRequest event, EntityRef character) {
        EntityRef item1 = event.getItem1();
        EntityRef item2 = event.getItem2();
        EntityRef item3 = event.getItem3();

        int item1Slot = inventoryManager.findSlotWithItem(character, item1);
        int item2Slot = inventoryManager.findSlotWithItem(character, item2);
        int item3Slot = inventoryManager.findSlotWithItem(character, item3);

        CraftInHandRecipeComponent component1 = item1.getComponent(CraftInHandRecipeComponent.class);
        CraftInHandRecipeComponent component2 = item2.getComponent(CraftInHandRecipeComponent.class);
        CraftInHandRecipeComponent component3 = item3.getComponent(CraftInHandRecipeComponent.class);

        CraftInHandRecipe.CraftInHandResult result = findResultForMatchingRecipe(component1, component2, component3);

        if (result != null) {
            boolean craftingSuccess = result.processCraftingForCharacter(character,
                    inventoryManager.getItemInSlot(character, item1Slot),
                    inventoryManager.getItemInSlot(character, item2Slot),
                    inventoryManager.getItemInSlot(character, item3Slot));
            if (craftingSuccess) {
                EntityRef resultEntity = entityManager.create(result.getResultPrefab());
                pickupBuilder.createPickupFor(resultEntity, character.getComponent(LocationComponent.class).getWorldPosition(), 200);
            }
        }
    }

    private CraftInHandRecipe.CraftInHandResult findResultForMatchingRecipe(CraftInHandRecipeComponent item1, CraftInHandRecipeComponent item2, CraftInHandRecipeComponent item3) {
        if (item1 == null && item2 == null && item3 == null)
            return null;
        for (CraftInHandRecipe recipe : recipes) {
            CraftInHandRecipe.CraftInHandResult result = recipe.getMatchingRecipeResult(item1, item2, item3);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
