// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.generator;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.StaticBlockFloraSpawnDefinition;
import org.terasology.was.generator.Blocks;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterPlugin
public class HerbTundraSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    public HerbTundraSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.TUNDRA.getId().toLowerCase(), 0.5f, 0.3f, "Herbalism:Herb",
                Arrays.asList(
                        new BlockUri("WoodAndStone:HerbGeneratedA")),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Snow")), null);
    }
}
