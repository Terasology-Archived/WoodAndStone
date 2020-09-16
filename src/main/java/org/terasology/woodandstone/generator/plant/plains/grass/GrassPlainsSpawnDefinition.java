// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.generator.plant.plains.grass;

import com.google.common.base.Predicate;
import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.LocalParameters;
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
public class GrassPlainsSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    public GrassPlainsSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.PLAINS.getId().toLowerCase(), 1f, 1f, "CoreAssets:TallGrass",
                Arrays.asList(new BlockUri("CoreAssets:TallGrass1"), new BlockUri("CoreAssets:TallGrass2"),
                        new BlockUri("CoreAssets:TallGrass3")),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Grass")),
                new Predicate<LocalParameters>() {
                    @Override
                    public boolean apply(LocalParameters input) {
                        return input.getHumidity() > 0.2f && input.getHumidity() < 0.8f
                                && input.getTemperature() > 0.3 && input.getTemperature() < 0.7f;
                    }
                });
    }
}
