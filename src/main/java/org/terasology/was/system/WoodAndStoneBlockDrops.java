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
package org.terasology.was.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.EntityBeingGenerated;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.was.component.BlockDropGrammarComponent;
import org.terasology.world.block.BlockUri;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WoodAndStoneBlockDrops implements ComponentSystem {
    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent
    public void overrideDropsForCoreBlocks(EntityBeingGenerated event, EntityRef entity) {
        BlockUri blockUri = event.getBlockUri();
        if (blockUri != null) {
            if (blockUri.equals(new BlockUri("Core", "Grass"))) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.blockDrops = Arrays.asList("Core:Dirt");
                dropGrammar.itemDrops = Arrays.asList("0.5|WoodAndStone:stone");
                event.addComponent(dropGrammar);
            } else if (blockUri.equals(new BlockUri("Core", "Dirt"))) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.blockDrops = Arrays.asList("Core:Dirt");
                dropGrammar.itemDrops = Arrays.asList("0.5|WoodAndStone:stone");
                event.addComponent(dropGrammar);
            } else if (blockUri.equals(new BlockUri("Core", "Stone"))) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.itemDrops = Arrays.asList("3*WoodAndStone:stone");
                event.addComponent(dropGrammar);
            } else if (blockUri.equals(new BlockUri("Core", "GreenLeaf"))) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.blockDrops = Arrays.asList("0.1|Core:OakSapling");
                dropGrammar.itemDrops = Arrays.asList("0.3|WoodAndStone:stick", "0.1|WoodAndStone:plantFibre");
                event.addComponent(dropGrammar);
            } else if (blockUri.equals(new BlockUri("Core", "OakBranch"))) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.itemDrops = Arrays.asList("WoodAndStone:stick");
                event.addComponent(dropGrammar);
            }
        }
    }
}
