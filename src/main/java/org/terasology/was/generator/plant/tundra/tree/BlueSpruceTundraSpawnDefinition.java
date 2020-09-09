// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.was.generator.plant.tundra.tree;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.anotherWorldPlants.tree.BlueSpruceGrowthDefinition;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.GrowthBasedPlantSpawnDefinition;
import org.terasology.was.generator.Blocks;

import java.util.Arrays;

@RegisterPlugin
public class BlueSpruceTundraSpawnDefinition extends GrowthBasedPlantSpawnDefinition {
    public BlueSpruceTundraSpawnDefinition() {
        super(PlantType.TREE, BlueSpruceGrowthDefinition.ID, AnotherWorldBiomes.TUNDRA.getId().toLowerCase(), 0.7f,
                0.3f,
                new BlockCollectionPredicate(Arrays.asList(Blocks.getBlock("CoreAssets:Snow"))));
    }
}
