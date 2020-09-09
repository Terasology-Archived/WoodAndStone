// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.generator.plant.plains.grass;

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
public class FlowerPlainsSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    public FlowerPlainsSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.PLAINS.getId().toLowerCase(), 0.5f, 1f, "CoreAssets:Flower",
                Arrays.asList(new BlockUri("CoreAssets:Dandelion"), new BlockUri("CoreAssets:Iris"),
                        new BlockUri("CoreAssets:Lavender"), new BlockUri("CoreAssets:RedClover"), new BlockUri(
                                "CoreAssets:RedFlower"),
                        new BlockUri("CoreAssets:Tulip"), new BlockUri("CoreAssets:YellowFlower")),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Grass")), null);
    }
}
