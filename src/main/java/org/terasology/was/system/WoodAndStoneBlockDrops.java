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

import org.terasology.core.logic.blockDropGrammar.BlockDropGrammarComponent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeEntityCreated;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gf.tree.PartOfTreeComponent;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockUri;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WoodAndStoneBlockDrops implements ComponentSystem {
    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent
    public void overrideDropsForCoreBlocks(BeforeEntityCreated event, EntityRef entity) {
        BlockUri blockUri = null;
        for (Component component : event.getResultComponents()) {
            if (component instanceof BlockComponent) {
                BlockComponent comp = (BlockComponent) component;
                blockUri = comp.getBlock().getBlockFamily().getURI();
            }
        }

        if (blockUri != null) {
            if (blockUri.equals(new BlockUri("Core", "Grass"))
                    || blockUri.equals(new BlockUri("Core", "Snow"))
                    || blockUri.equals(new BlockUri("Core", "Dirt"))) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.itemDrops = Arrays.asList("0.5|WoodAndStone:stone", "0.1|WoodAndStone:flint");
                event.addComponent(dropGrammar);
            } else if (blockUri.equals(new BlockUri("Core", "Stone"))) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.itemDrops = Arrays.asList("3*WoodAndStone:stone");
                event.addComponent(dropGrammar);
            } else if (blockUri.getNormalisedModuleName().equalsIgnoreCase("plantpack")
                    && blockUri.getNormalisedFamilyName().endsWith("leaf")) {
                // A bit of hacking, assuming they follow naming pattern
                String familyName = blockUri.getNormalisedFamilyName();
                String treeType = familyName.substring(0, familyName.length() - 4);
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.blockDrops = Arrays.asList("0.1|PlantPack:" + treeType + "Sapling");
                dropGrammar.itemDrops = Arrays.asList("0.3|WoodAndStone:stick", "0.1|WoodAndStone:twig");
                event.addComponent(dropGrammar);
            }
        }

        PartOfTreeComponent component = getPartOfTree(event.getResultComponents());
        if (component != null) {
            if (component.part == PartOfTreeComponent.Part.BRANCH) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.itemDrops = Arrays.asList("WoodAndStone:stick");
                event.addComponent(dropGrammar);
            } else if (component.part == PartOfTreeComponent.Part.TRUNK) {
                BlockDropGrammarComponent dropGrammar = new BlockDropGrammarComponent();
                dropGrammar.blockDrops = Arrays.asList("WoodAndStone:TreeLog");
                dropGrammar.itemDrops = Arrays.asList("0.1|WoodAndStone:Resin");
                event.addComponent(dropGrammar);
            }
        }
    }

    private PartOfTreeComponent getPartOfTree(Iterable<Component> resultComponents) {
        for (Component resultComponent : resultComponents) {
            if (resultComponent instanceof PartOfTreeComponent) {
                return (PartOfTreeComponent) resultComponent;
            }
        }
        return null;
    }
}
