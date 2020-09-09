// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.system.recipe.workstation;

import org.terasology.crafting.component.CraftingStationIngredientComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.inventory.logic.InventoryComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.math.geom.Vector3i;
import org.terasology.workstation.process.WorkstationInventoryUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleUpgradeRecipe implements UpgradeRecipe {
    private final Map<String, Integer> ingredientsMap = new LinkedHashMap<>();
    private final String resultStationType;
    private final String resultStationPrefab;
    private final String resultBlockUri;

    private final InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);

    public SimpleUpgradeRecipe(String resultStationType, String resultStationPrefab, String resultBlockUri) {
        this.resultStationType = resultStationType;
        this.resultStationPrefab = resultStationPrefab;
        this.resultBlockUri = resultBlockUri;
    }

    public void addIngredient(String type, int count) {
        ingredientsMap.put(type, count);
    }

    @Override
    public boolean isUpgradeComponent(EntityRef item) {
        CraftingStationIngredientComponent ingredient = item.getComponent(CraftingStationIngredientComponent.class);
        return ingredient != null && ingredientsMap.containsKey(ingredient.type);
    }

    @Override
    public UpgradeResult getMatchingUpgradeResult(EntityRef station) {
        List<Integer> resultSlots = new LinkedList<>();
        for (Map.Entry<String, Integer> ingredientCount : ingredientsMap.entrySet()) {
            int slotNo = hasItem(station, ingredientCount.getKey(), ingredientCount.getValue());
            if (slotNo != -1) {
                resultSlots.add(slotNo);
            } else {
                return null;
            }
        }

        return new Result(resultSlots);
    }

    private int hasItem(EntityRef station, String itemType, int count) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(station, "UPGRADE")) {
            if (hasItemInSlot(station, itemType, slot, count)) {
                return slot;
            }
        }

        return -1;
    }

    private boolean hasItemInSlot(EntityRef station, String itemType, int slot, int count) {
        EntityRef item = inventoryManager.getItemInSlot(station, slot);
        CraftingStationIngredientComponent component = item.getComponent(CraftingStationIngredientComponent.class);
        return component != null && component.type.equals(itemType) && item.getComponent(ItemComponent.class).stackCount >= count;
    }


    private class Result implements UpgradeResult {
        private final List<Integer> items;

        public Result(List<Integer> slots) {
            items = slots;
        }

        @Override
        public String getResultStationType() {
            return resultStationType;
        }

        @Override
        public EntityRef processUpgrade(EntityRef station) {
            if (!validateCreation(station)) {
                return EntityRef.NULL;
            }

            int index = 0;
            for (Map.Entry<String, Integer> ingredientCount : ingredientsMap.entrySet()) {
                inventoryManager.removeItem(station, station, inventoryManager.getItemInSlot(station,
                        items.get(index)), true, ingredientCount.getValue());
                index++;
            }

            WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);

            Block block = blockManager.getBlock(resultBlockUri);
            Region3i region = station.getComponent(BlockRegionComponent.class).region;
            for (Vector3i location : region) {
                worldProvider.setBlock(location, block);
            }

            final EntityRef newStation = entityManager.create(resultStationPrefab);
            InventoryComponent oldStationInventory = station.getComponent(InventoryComponent.class);
            InventoryComponent newStationInventory = newStation.getComponent(InventoryComponent.class);

            moveItems(station, newStation, oldStationInventory, newStationInventory, "UPGRADE");
            moveItems(station, newStation, oldStationInventory, newStationInventory, "TOOL");
            moveItems(station, newStation, oldStationInventory, newStationInventory, "INPUT");
            moveItems(station, newStation, oldStationInventory, newStationInventory, "OUTPUT");

            for (int i = 0; i < oldStationInventory.itemSlots.size(); i++) {
                oldStationInventory.itemSlots.set(i, EntityRef.NULL);
            }

            newStation.saveComponent(newStationInventory);

            station.destroy();

            newStation.addComponent(new BlockRegionComponent(region));
            newStation.addComponent(new LocationComponent(region.center()));

            return newStation;
        }

        private void moveItems(EntityRef oldStation, EntityRef newStation, InventoryComponent oldStationInventory,
                               InventoryComponent newStationInventory, String slotType) {
            List<Integer> inSlots = WorkstationInventoryUtils.getAssignedSlots(oldStation, slotType);
            List<Integer> outSlots = WorkstationInventoryUtils.getAssignedSlots(newStation, slotType);
            for (int i = 0; i < inSlots.size(); i++) {
                int slotFrom = inSlots.get(i);
                int slotTo = outSlots.get(i);
                newStationInventory.itemSlots.set(slotTo, oldStationInventory.itemSlots.get(slotFrom));
            }
        }

        private boolean validateCreation(EntityRef station) {
            int index = 0;
            for (Map.Entry<String, Integer> ingredientCount : ingredientsMap.entrySet()) {
                if (!hasItemInSlot(station, ingredientCount.getKey(), items.get(index), ingredientCount.getValue())) {
                    return false;
                }
                index++;
            }

            return true;
        }
    }
}
