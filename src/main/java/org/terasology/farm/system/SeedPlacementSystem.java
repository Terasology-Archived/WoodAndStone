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
package org.terasology.farm.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.farm.component.FarmSoilComponent;
import org.terasology.farm.component.SeedComponent;
import org.terasology.farm.event.BeforeSeedPlanted;
import org.terasology.farm.event.SeedPlanted;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.placement.PlaceBlocks;

import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SeedPlacementSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void planting(ActivateEvent event, EntityRef item, SeedComponent seed) {
        boolean consume = true;
        EntityRef target = event.getTarget();
        // Clicked on top of soil
        if (Side.inDirection(event.getHitNormal()) == Side.TOP && target.hasComponent(FarmSoilComponent.class)) {
            BeforeSeedPlanted plantEvent = new BeforeSeedPlanted(event.getInstigator(), target);
            item.send(plantEvent);
            if (!plantEvent.isConsumed()) {
                Block blockPlaced = seed.blockPlaced;
                Vector3f location = event.getTargetLocation();
                location.add(new Vector3f(0, 1, 0));
                Vector3i blockLocation = new Vector3i(location, 0.5f);
                PlaceBlocks placeBlocks = new PlaceBlocks(blockLocation, blockPlaced);
                worldProvider.getWorldEntity().send(placeBlocks);
                if (!placeBlocks.isConsumed()) {
                    item.send(new SeedPlanted(blockLocation));
                    consume = false;
                }
            }
        }

        if (consume) {
            event.consume();
        }
    }
}
