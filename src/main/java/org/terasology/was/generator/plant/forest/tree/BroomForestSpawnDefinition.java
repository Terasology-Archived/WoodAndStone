// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.generator.plant.forest.tree;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.anotherWorldPlants.tree.BroomGrowthDefinition;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.GrowthBasedPlantSpawnDefinition;
import org.terasology.was.generator.Blocks;

import java.util.Arrays;

@RegisterPlugin
public class BroomForestSpawnDefinition extends GrowthBasedPlantSpawnDefinition {
    public BroomForestSpawnDefinition() {
        super(PlantType.TREE, BroomGrowthDefinition.ID, AnotherWorldBiomes.FOREST.getId().toLowerCase(), 0.6f, 0.6f,
                new BlockCollectionPredicate(Arrays.asList(Blocks.getBlock("CoreAssets:Grass"))));
    }
}
