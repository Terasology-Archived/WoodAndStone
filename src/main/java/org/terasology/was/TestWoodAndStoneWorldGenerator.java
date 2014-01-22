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
import org.terasology.anotherWorld.coreBiome.DesertBiome;
import org.terasology.anotherWorld.coreBiome.ForestBiome;
import org.terasology.anotherWorld.coreBiome.PlainsBiome;
import org.terasology.anotherWorld.coreBiome.TundraBiome;
import org.terasology.anotherWorld.decorator.BeachDecorator;
import org.terasology.anotherWorld.decorator.layering.DefaultLayersDefinition;
import org.terasology.anotherWorld.decorator.layering.LayeringDecorator;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.liquid.LiquidType;

import java.util.Collection;
import java.util.Collections;

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
        setSeaLevel(50);

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        final Block mantle = blockManager.getBlock("Core:MantleStone");
        final Block stone = blockManager.getBlock("Core:Stone");
        final Block water = blockManager.getBlock("Core:Water");
        final Block sand = blockManager.getBlock("Core:Sand");
        final Block dirt = blockManager.getBlock("Core:Dirt");
        final Block grass = blockManager.getBlock("Core:Grass");
        final Block snow = blockManager.getBlock("Core:Snow");

        setLandscapeGenerator(
                new PerlinLandscapeGenerator(0.3f, mantle, stone, water, LiquidType.WATER));

        addChunkDecorator(
                new BeachDecorator(Collections.singleton(stone), sand));

        setupLayers(stone, sand, dirt, grass, snow);
    }

    private void setupLayers(Block stone, Block sand, Block dirt, Block grass, Block snow) {
        Collection<Block> replacedBlocks = Collections.singleton(stone);

        LayeringDecorator layering = new LayeringDecorator();

        DefaultLayersDefinition desertDef = new DefaultLayersDefinition();
        desertDef.addLayerDefinition(new PDist(3, 1), replacedBlocks, sand, false);
        desertDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        layering.addBiomeLayers(DesertBiome.ID, desertDef);

        DefaultLayersDefinition forestAndPlainsDef = new DefaultLayersDefinition();
        forestAndPlainsDef.addLayerDefinition(new PDist(1, 0), replacedBlocks, grass, false);
        forestAndPlainsDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        layering.addBiomeLayers(ForestBiome.ID, forestAndPlainsDef);
        layering.addBiomeLayers(PlainsBiome.ID, forestAndPlainsDef);

        DefaultLayersDefinition tundraDef = new DefaultLayersDefinition();
        tundraDef.addLayerDefinition(new PDist(1, 0), replacedBlocks, snow, false);
        tundraDef.addLayerDefinition(new PDist(4, 2), replacedBlocks, dirt, true);
        layering.addBiomeLayers(TundraBiome.ID, tundraDef);

        addChunkDecorator(layering);
    }
}
