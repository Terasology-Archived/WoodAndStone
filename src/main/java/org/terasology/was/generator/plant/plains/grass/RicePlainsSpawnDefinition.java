// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.generator.plant.plains.grass;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.anotherWorldPlants.crop.RiceGrowthDefinition;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.GrowthBasedPlantSpawnDefinition;
import org.terasology.was.generator.Blocks;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterPlugin
public class RicePlainsSpawnDefinition extends GrowthBasedPlantSpawnDefinition {
    public RicePlainsSpawnDefinition() {
        super(PlantType.GRASS, RiceGrowthDefinition.ID, AnotherWorldBiomes.PLAINS.getId().toLowerCase(), 0.8f, 0.3f,
                new BlockCollectionPredicate(Arrays.asList(Blocks.getBlock("CoreAssets:Grass"))));
    }
}
