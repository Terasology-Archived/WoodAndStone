// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.generator.plant.forest.grass;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.growingflora.PlantType;
import org.terasology.growingflora.generator.StaticBlockFloraSpawnDefinition;
import org.terasology.woodandstone.generator.Blocks;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterPlugin
public class ShroomForestSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    public ShroomForestSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.FOREST.getId().toLowerCase(), 0.5f, 0.3f, "CoreAssets:Shroom",
                Arrays.asList(new BlockUri("CoreAssets:BigBrownShroom"), new BlockUri("CoreAssets:BrownShroom"),
                        new BlockUri("CoreAssets:RedShroom")),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Grass")), null);
    }
}
