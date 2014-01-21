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

import org.terasology.anotherWorld.PerlinLandscapeGenerator;
import org.terasology.anotherWorld.PluggableWorldGenerator;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.liquid.LiquidType;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterWorldGenerator(id = "woodAndStoneTest", displayName = "Test Wood and Stone", description = "Generates the world for playing 'Wood and Stone' content mod")
public class TestWoodAndStoneWorldGenerator extends PluggableWorldGenerator {

    public TestWoodAndStoneWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected void appendGenerators() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        final Block mantle = blockManager.getBlock("Core:MantleStone");
        final Block stone = blockManager.getBlock("Core:Stone");
        final Block water = blockManager.getBlock("Core:Water");

        setLandscapeGenerator(
                new PerlinLandscapeGenerator(0.6f, mantle, stone, water, LiquidType.WATER));
    }
}
