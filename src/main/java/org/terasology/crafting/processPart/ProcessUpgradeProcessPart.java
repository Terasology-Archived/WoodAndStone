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
package org.terasology.crafting.processPart;

import org.terasology.crafting.component.CraftingStationComponent;
import org.terasology.crafting.event.CraftingStationUpgraded;
import org.terasology.crafting.system.recipe.workstation.UpgradeRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.workstation.process.ProcessPart;

public class ProcessUpgradeProcessPart implements ProcessPart {
    private UpgradeRecipe upgradeRecipe;

    public ProcessUpgradeProcessPart(UpgradeRecipe upgradeRecipe) {
        this.upgradeRecipe = upgradeRecipe;
    }

    @Override
    public boolean validate(EntityRef instigator, EntityRef workstation) {
        return false;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation) {
        final CraftingStationComponent craftingStation = workstation.getComponent(CraftingStationComponent.class);
        if (craftingStation == null) {
            return;
        }

        final UpgradeRecipe.UpgradeResult result = upgradeRecipe.getMatchingUpgradeResult(workstation, 0, craftingStation.upgradeSlots);
        if (result != null) {
            EntityRef newStation = result.processUpgrade(workstation);
            instigator.send(new CraftingStationUpgraded(newStation));
        }
    }
}
