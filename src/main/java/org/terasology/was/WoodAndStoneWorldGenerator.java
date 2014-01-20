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
package org.terasology.was;

import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.core.world.generator.chunkGenerators.BasicHMTerrainGenerator;
import org.terasology.core.world.generator.chunkGenerators.FloraGenerator;
import org.terasology.core.world.generator.chunkGenerators.ForestGenerator;
import org.terasology.core.world.liquid.LiquidsGenerator;
import org.terasology.engine.SimpleUri;
import org.terasology.gf.tree.TreeGrowingSystem;
import org.terasology.world.generator.RegisterWorldGenerator;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterWorldGenerator(id = "woodAndStone", displayName = "Wood and Stone", description = "Generates the world for playing 'Wood and Stone' content mod")
public class WoodAndStoneWorldGenerator extends AbstractBaseWorldGenerator {

    public WoodAndStoneWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public void initialize() {
        register(new BasicHMTerrainGenerator());
        register(new FloraGenerator());
        register(new LiquidsGenerator());
        ForestGenerator forestGenerator = new ForestGenerator();
        TreeGrowingSystem.setupForestGenerator(forestGenerator);
        register(forestGenerator);
    }
}
