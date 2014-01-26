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
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.manager.GUIManager;
import org.terasology.registry.In;
import org.terasology.workstation.component.CraftingStationComponent;
import org.terasology.workstation.event.OpenCraftingWorkstationRequest;
import org.terasology.workstation.ui.UICraftOnStation;

import javax.vecmath.Vector2f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CraftingWorkstationClientSystem implements ComponentSystem {
    @In
    private GUIManager guiManager;

    @Override
    public void initialise() {
        guiManager.registerWindow("WoodAndStone:CraftingStation", UICraftOnStation.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void openCraftingWorkstationWindow(OpenCraftingWorkstationRequest event, EntityRef workStation) {
        final UICraftOnStation uiCraftOnStation = (UICraftOnStation) guiManager.openWindow("WoodAndStone:CraftingStation");
        final CraftingStationComponent craftingStationComponent = workStation.getComponent(CraftingStationComponent.class);
        uiCraftOnStation.setCraftingStation(workStation, craftingStationComponent.type, craftingStationComponent.workstationUITexture, new Vector2f(0, 0),
                craftingStationComponent.upgradeSlots, craftingStationComponent.toolSlots, craftingStationComponent.ingredientSlots);
    }
}
