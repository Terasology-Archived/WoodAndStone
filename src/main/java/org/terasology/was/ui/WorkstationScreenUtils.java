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
package org.terasology.was.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.workstation.component.WorkstationInventoryComponent;

public final class WorkstationScreenUtils {
    private WorkstationScreenUtils() {
    }

    public static void setupInventoryGrid(EntityRef workstation, InventoryGrid inventoryGrid, String type) {
        WorkstationInventoryComponent workstationInventory = workstation.getComponent(WorkstationInventoryComponent.class);
        WorkstationInventoryComponent.SlotAssignment assignment = workstationInventory.slotAssignments.get(type);

        inventoryGrid.setTargetEntity(workstation);
        inventoryGrid.setCellOffset(assignment.slotStart);
        inventoryGrid.setMaxCellCount(assignment.slotCount);
    }
}
