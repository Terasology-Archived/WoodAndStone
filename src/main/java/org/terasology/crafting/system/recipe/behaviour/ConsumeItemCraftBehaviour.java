// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.behaviour;

import com.google.common.base.Predicate;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.ItemSlotIngredientRenderer;
import org.terasology.crafting.system.recipe.render.MultiplyFunction;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.InventoryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConsumeItemCraftBehaviour implements IngredientCraftBehaviour<EntityRef> {
    private final Predicate<EntityRef> matcher;
    private final int count;
    private final InventorySlotResolver resolver;

    public ConsumeItemCraftBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
        this.matcher = matcher;
        this.count = count;
        this.resolver = resolver;
    }

    @Override
    public boolean isValidAnyAmount(EntityRef ingredient) {
        return matcher.apply(ingredient);
    }

    protected String getParameter(List<Integer> slots, EntityRef item) {
        StringBuilder sb = new StringBuilder();
        for (int slot : slots) {
            sb.append(slot).append(",");
        }

        return sb.replace(sb.length() - 1, sb.length(), "").toString();
    }

    protected List<Integer> getSlots(String parameter) {
        String[] split = parameter.split(",");
        List<Integer> result = new LinkedList<>();
        for (String slot : split) {
            result.add(Integer.parseInt(slot));
        }

        return result;
    }

    @Override
    public List<String> getValidToCraft(EntityRef entity, int multiplier) {
        Map<EntityRef, Integer> itemCounts = new LinkedHashMap<>();
        Map<EntityRef, List<Integer>> slots = new LinkedHashMap<>();

        for (int slot : resolver.getSlots(entity)) {
            EntityRef item = InventoryUtils.getItemAt(entity, slot);
            if (matcher.apply(item)) {
                boolean defined = false;
                for (Map.Entry<EntityRef, Integer> itemCount : itemCounts.entrySet()) {
                    EntityRef sameItem = itemCount.getKey();
                    if (InventoryUtils.isSameItem(sameItem, item)) {
                        itemCount.setValue(itemCount.getValue() + InventoryUtils.getStackCount(item));
                        slots.get(sameItem).add(slot);
                        defined = true;
                        break;
                    }
                }

                if (!defined) {
                    itemCounts.put(item, InventoryUtils.getStackCount(item));
                    slots.put(item, new ArrayList<>(Arrays.asList(slot)));
                }
            }
        }

        List<String> result = new LinkedList<>();
        for (Map.Entry<EntityRef, Integer> itemCount : itemCounts.entrySet()) {
            if (itemCount.getValue() >= count * multiplier) {
                EntityRef item = itemCount.getKey();
                result.add(getParameter(slots.get(item), item));
            }
        }

        return result;
    }

    private boolean isValidToCraft(EntityRef entity, List<Integer> slots, int multiplier) {
        int sum = getItemSum(entity, slots);

        return sum >= count * multiplier;
    }

    private int getItemSum(EntityRef entity, List<Integer> slots) {
        int sum = 0;
        for (int slot : slots) {
            EntityRef ingredient = InventoryUtils.getItemAt(entity, slot);
            if (matcher.apply(ingredient)) {
                ItemComponent itemComponent = ingredient.getComponent(ItemComponent.class);
                if (itemComponent != null) {
                    sum += itemComponent.stackCount;
                }
            }
        }
        return sum;
    }

    @Override
    public boolean isValidToCraft(EntityRef entity, String parameter, int multiplier) {
        return isValidToCraft(entity, getSlots(parameter), multiplier);
    }

    @Override
    public int getMaxMultiplier(EntityRef entity, String parameter) {
        return getItemSum(entity, getSlots(parameter)) / count;
    }

    @Override
    public CraftIngredientRenderer getRenderer(EntityRef entity, String parameter) {
        ItemSlotIngredientRenderer renderer = new ItemSlotIngredientRenderer();
        renderer.update(entity, getSlots(parameter).get(0), new MultiplyFunction(count));
        return renderer;
    }

    @Override
    public void processIngredient(EntityRef instigator, EntityRef entity, String parameter, int multiplier) {
        List<EntityRef> items = new LinkedList<>();
        for (int slot : getSlots(parameter)) {
            items.add(InventoryUtils.getItemAt(entity, slot));
        }

        CoreRegistry.get(InventoryManager.class).removeItem(entity, instigator, items, true, count * multiplier);
    }
}
