/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.was.generator.plant.forest.grass;

import com.google.common.base.Predicate;
import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.StaticBlockFloraSpawnDefinition;
import org.terasology.was.generator.Blocks;
import org.terasology.world.block.BlockUri;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterPlugin
public class GrassForestSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    public GrassForestSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.FOREST.getId().toLowerCase(), 1f, 0.7f, "CoreAssets:TallGrass",
                Arrays.asList(new BlockUri("CoreAssets:TallGrass1"), new BlockUri("CoreAssets:TallGrass2"), new BlockUri("CoreAssets:TallGrass3")),
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
