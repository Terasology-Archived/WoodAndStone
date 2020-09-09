// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.behaviour;

import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.system.FluidManager;
import org.terasology.math.TeraMath;

import java.util.LinkedList;
import java.util.List;

public class ConsumeFluidBehaviour implements IngredientCraftBehaviour<String> {
    private final String fluidType;
    private final float volume;
    private final InventorySlotResolver resolver;

    public ConsumeFluidBehaviour(String fluidType, float volume, InventorySlotResolver resolver) {
        this.fluidType = fluidType.toLowerCase();
        this.volume = volume;
        this.resolver = resolver;
    }

    @Override
    public int getMaxMultiplier(EntityRef entity, String slot) {
        FluidInventoryComponent fluidInventory = entity.getComponent(FluidInventoryComponent.class);
        FluidComponent fluid = fluidInventory.fluidSlots.get(Integer.parseInt(slot)).getComponent(FluidComponent.class);
        return TeraMath.floorToInt(fluid.volume / volume);
    }

    @Override
    public boolean isValidAnyAmount(String ingredient) {
        return fluidType.equals(ingredient);
    }

    @Override
    public List<String> getValidToCraft(EntityRef entity, int multiplier) {
        List<String> result = new LinkedList<>();
        for (int slot : resolver.getSlots(entity)) {
            if (isValidToCraft(entity, slot, multiplier)) {
                result.add(String.valueOf(slot));
            }
        }

        return result;
    }

    private boolean isValidToCraft(EntityRef entity, int slot, int multiplier) {
        FluidInventoryComponent fluidInventory = entity.getComponent(FluidInventoryComponent.class);
        FluidComponent fluid = fluidInventory.fluidSlots.get(slot).getComponent(FluidComponent.class);
        return fluid != null && fluid.fluidType.equals(fluidType) && fluid.volume >= volume * multiplier;
    }

    @Override
    public boolean isValidToCraft(EntityRef entity, String slot, int multiplier) {
        return isValidToCraft(entity, Integer.parseInt(slot), multiplier);
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, String slot) {
        return null;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, String slot, int multiplier) {
        FluidManager fluidManager = CoreRegistry.get(FluidManager.class);
        fluidManager.removeFluid(instigator, entity, Integer.parseInt(slot), fluidType, volume * multiplier);
    }
}
