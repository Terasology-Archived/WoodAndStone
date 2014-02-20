/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.system.recipe.behaviour;

import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.system.FluidManager;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;

import java.util.LinkedList;
import java.util.List;

public class ConsumeFluidBehaviour implements IngredientCraftBehaviour<String, Integer> {
    private String fluidType;
    private float volume;
    private InventorySlotResolver resolver;

    public ConsumeFluidBehaviour(String fluidType, float volume, InventorySlotResolver resolver) {
        this.fluidType = fluidType;
        this.volume = volume;
        this.resolver = resolver;
    }

    @Override
    public int getMaxMultiplier(EntityRef entity, Integer slot) {
        FluidInventoryComponent fluidInventory = entity.getComponent(FluidInventoryComponent.class);
        FluidComponent fluid = fluidInventory.fluidSlots.get(slot).getComponent(FluidComponent.class);
        return TeraMath.floorToInt(fluid.volume / volume);
    }

    @Override
    public boolean isValidAnyAmount(String ingredient) {
        return fluidType.equals(ingredient);
    }

    @Override
    public List<Integer> getValidToCraft(EntityRef entity, int multiplier) {
        List<Integer> result = new LinkedList<>();
        for (int slot : resolver.getSlots(entity)) {
            if (isValidToCraft(entity, slot, multiplier)) {
                result.add(slot);
            }
        }

        return result;
    }

    @Override
    public boolean isValidToCraft(EntityRef entity, Integer slot, int multiplier) {
        FluidInventoryComponent fluidInventory = entity.getComponent(FluidInventoryComponent.class);
        FluidComponent fluid = fluidInventory.fluidSlots.get(slot).getComponent(FluidComponent.class);
        if (fluid != null && fluid.fluidType.equals(fluidType) && fluid.volume >= volume * multiplier) {
            return true;
        }
        return false;
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, Integer slot) {
        return null;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, Integer slot, int multiplier) {
        FluidManager fluidManager = CoreRegistry.get(FluidManager.class);
        fluidManager.removeFluid(instigator, entity, slot, fluidType, volume * multiplier);
    }
}
