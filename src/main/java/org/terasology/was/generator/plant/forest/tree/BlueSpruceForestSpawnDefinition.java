// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.generator.plant.forest.tree;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.anotherWorldPlants.tree.BlueSpruceGrowthDefinition;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.GrowthBasedPlantSpawnDefinition;
import org.terasology.was.generator.Blocks;

import java.util.Arrays;

@RegisterPlugin
public class BlueSpruceForestSpawnDefinition extends GrowthBasedPlantSpawnDefinition {
    public BlueSpruceForestSpawnDefinition() {
        super(PlantType.TREE, BlueSpruceGrowthDefinition.ID, AnotherWorldBiomes.FOREST.getId().toLowerCase(), 0.2f,
                0.4f,
                new BlockCollectionPredicate(Arrays.asList(Blocks.getBlock("CoreAssets:Grass"))));
    }
}
