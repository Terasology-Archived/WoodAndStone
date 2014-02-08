package org.terasology.workstation.system.recipe;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.component.CraftingStationComponent;
import org.terasology.workstation.component.CraftingStationIngredientComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleUpgradeRecipe implements UpgradeRecipe {
    private Map<String, Integer> ingredientsMap = new LinkedHashMap<>();
    private String resultStationType;
    private String resultStationPrefab;
    private String resultBlockUri;

    private InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);

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
    public UpgradeResult getMatchingUpgradeResult(EntityRef station, int upgradeSlotFrom, int upgradeSlotCount) {
        List<Integer> resultSlots = new LinkedList<>();
        for (Map.Entry<String, Integer> ingredientCount : ingredientsMap.entrySet()) {
            int slotNo = hasItem(station, ingredientCount.getKey(), ingredientCount.getValue(), upgradeSlotFrom, upgradeSlotFrom + upgradeSlotCount);
            if (slotNo != -1) {
                resultSlots.add(slotNo);
            } else {
                return null;
            }
        }

        return new Result(resultSlots);
    }

    private int hasItem(EntityRef station, String itemType, int count, int fromSlot, int toSlot) {
        for (int i = fromSlot; i < toSlot; i++) {
            if (hasItemInSlot(station, itemType, i, count)) {
                return i;
            }
        }

        return -1;
    }

    private boolean hasItemInSlot(EntityRef station, String itemType, int slot, int count) {
        EntityRef item = inventoryManager.getItemInSlot(station, slot);
        CraftingStationIngredientComponent component = item.getComponent(CraftingStationIngredientComponent.class);
        if (component != null && component.type.equals(itemType) && item.getComponent(ItemComponent.class).stackCount >= count) {
            return true;
        }
        return false;
    }


    private class Result implements UpgradeResult {
        private List<Integer> items;

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
                RemoveItemAction removeAction = new RemoveItemAction(station, inventoryManager.getItemInSlot(station, items.get(index)), true, ingredientCount.getValue());
                station.send(removeAction);
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
            CraftingStationComponent oldStationSettings = station.getComponent(CraftingStationComponent.class);
            CraftingStationComponent newStationSettings = newStation.getComponent(CraftingStationComponent.class);

            InventoryComponent oldStationInventory = station.getComponent(InventoryComponent.class);
            InventoryComponent newStationInventory = newStation.getComponent(InventoryComponent.class);

            // Moving upgrades
            for (int i = 0; i < oldStationSettings.upgradeSlots; i++) {
                newStationInventory.itemSlots.set(i, oldStationInventory.itemSlots.get(i));
            }
            // Moving tools
            for (int i = 0; i < oldStationSettings.toolSlots; i++) {
                newStationInventory.itemSlots.set(newStationSettings.upgradeSlots + i,
                        oldStationInventory.itemSlots.get(oldStationSettings.upgradeSlots + i));
            }
            // Moving ingredients
            for (int i = 0; i < oldStationSettings.ingredientSlots; i++) {
                newStationInventory.itemSlots.set(newStationSettings.upgradeSlots + newStationSettings.toolSlots + i,
                        oldStationInventory.itemSlots.get(oldStationSettings.upgradeSlots + oldStationSettings.toolSlots + i));
            }
            // Moving output
            newStationInventory.itemSlots.set(newStationSettings.upgradeSlots + newStationSettings.toolSlots + newStationSettings.ingredientSlots,
                    oldStationInventory.itemSlots.get(oldStationSettings.upgradeSlots + oldStationSettings.toolSlots + oldStationSettings.ingredientSlots));

            newStation.saveComponent(newStationInventory);

            for (int i = 0; i < oldStationInventory.itemSlots.size(); i++) {
                oldStationInventory.itemSlots.set(i, EntityRef.NULL);
            }

            station.destroy();

            newStation.addComponent(new BlockRegionComponent(region));
            newStation.addComponent(new LocationComponent(region.center()));

            return newStation;
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
