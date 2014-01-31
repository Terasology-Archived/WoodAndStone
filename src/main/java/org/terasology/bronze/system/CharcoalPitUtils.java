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
package org.terasology.bronze.system;

import org.terasology.bronze.component.CharcoalPitComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.workstation.component.CraftingStationIngredientComponent;

public final class CharcoalPitUtils {
    private CharcoalPitUtils() {
    }

    public static int getResultCharcoalCount(int logCount, EntityRef charcoalPitEntity) {
        CharcoalPitComponent charcoalPit = charcoalPitEntity.getComponent(CharcoalPitComponent.class);
        int min = charcoalPit.minimumLogCount;
        int max = charcoalPit.maximumLogCount;

        return Math.round(1f * logCount * logCount / max);
    }

    public static int getLogCount(SlotBasedInventoryManager inventoryManager, EntityRef charcoalPitEntity) {
        CharcoalPitComponent charcoalPit = charcoalPitEntity.getComponent(CharcoalPitComponent.class);
        int logCount = 0;
        for (int i = 0; i < charcoalPit.inputSlotCount; i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(charcoalPitEntity, i);
            if (itemInSlot.hasComponent(CraftingStationIngredientComponent.class)
                    && itemInSlot.getComponent(CraftingStationIngredientComponent.class).type.equals("WoodAndStone:wood")) {
                logCount += itemInSlot.getComponent(ItemComponent.class).stackCount;
            } else {
                return -1;
            }
        }
        return logCount;
    }

    public static boolean canBurnCharcoal(SlotBasedInventoryManager inventoryManager, int logCount, EntityRef charcoalPitEntity) {
        CharcoalPitComponent charcoalPit = charcoalPitEntity.getComponent(CharcoalPitComponent.class);

        int resultCharcoalCount = getResultCharcoalCount(logCount, charcoalPitEntity);
        int availableCharcoalPlace = 0;
        for (int i = charcoalPit.inputSlotCount; i < charcoalPit.inputSlotCount + charcoalPit.outputSlotCount; i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(charcoalPitEntity, i);
            if (!itemInSlot.exists()) {
                availableCharcoalPlace += 99;
            }
        }

        return logCount >= charcoalPit.minimumLogCount && logCount <= charcoalPit.maximumLogCount && resultCharcoalCount <= availableCharcoalPlace;
    }
}
