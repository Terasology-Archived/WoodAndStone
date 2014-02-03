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
package org.terasology.was.generator;

import org.terasology.anotherWorld.PerlinLandscapeGenerator;
import org.terasology.anotherWorld.PluggableWorldGenerator;
import org.terasology.anotherWorld.coreBiome.AlpineBiome;
import org.terasology.anotherWorld.coreBiome.DesertBiome;
import org.terasology.anotherWorld.coreBiome.ForestBiome;
import org.terasology.anotherWorld.coreBiome.PlainsBiome;
import org.terasology.anotherWorld.coreBiome.TaigaBiome;
import org.terasology.anotherWorld.coreBiome.TundraBiome;
import org.terasology.anotherWorld.decorator.BeachDecorator;
import org.terasology.anotherWorld.decorator.BlockCollectionFilter;
import org.terasology.anotherWorld.decorator.CaveDecorator;
import org.terasology.anotherWorld.decorator.layering.DefaultLayersDefinition;
import org.terasology.anotherWorld.decorator.layering.LayeringConfig;
import org.terasology.anotherWorld.decorator.layering.LayeringDecorator;
import org.terasology.anotherWorld.decorator.ore.OreDecorator;
import org.terasology.anotherWorld.util.Filter;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.anotherWorld.util.alpha.IdentityAlphaFunction;
import org.terasology.anotherWorld.util.alpha.MinMaxAlphaFunction;
import org.terasology.anotherWorld.util.alpha.PowerAlphaFunction;
import org.terasology.engine.SimpleUri;
import org.terasology.gf.generator.FloraFeatureGenerator;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.liquid.LiquidType;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterWorldGenerator(id = "overTheAges", displayName = "Over the Ages", description = "Generates the world for playing 'Over the Ages' content mods.")
public class WoodAndStoneWorldGenerator extends PluggableWorldGenerator {
    private BlockManager blockManager;

    public WoodAndStoneWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected void setupGenerator() {
        setSeaLevel(100);
        setMaxLevel(200);

        // Make sure that area on the sea level is not dry, this will prevent deserts spawning next to sea
        setHumidityFunction(
                new MinMaxAlphaFunction(IdentityAlphaFunction.singleton(), 0.3f, 1f));
        // Make sure that area on the sea level in not too cold, so that colder areas (with snow) will
        // only exist in higher Y-levels
        setTemperatureFunction(
                new MinMaxAlphaFunction(IdentityAlphaFunction.singleton(), 0.4f, 1f));

        blockManager = CoreRegistry.get(BlockManager.class);

        final Block mantle = blockManager.getBlock("Core:MantleStone");
        final Block stone = blockManager.getBlock("Core:Stone");
        final Block water = blockManager.getBlock("Core:Water");
        final Block sand = blockManager.getBlock("Core:Sand");
        final Block dirt = blockManager.getBlock("Core:Dirt");
        final Block grass = blockManager.getBlock("Core:Grass");
        final Block snow = blockManager.getBlock("Core:Snow");
        final Block ice = blockManager.getBlock("Core:Ice");

        setLandscapeProvider(
                new PerlinLandscapeGenerator(
                        0.4f,
                        // Make the lowlands a bit more common than higher areas (using PowerAlphaFunction)
                        new PowerAlphaFunction(IdentityAlphaFunction.singleton(), 1.3f),
                        // Smoothen the terrain a bit
                        0.5f, new PowerAlphaFunction(IdentityAlphaFunction.singleton(), 0.5f)));

        // Setup biome terrain layers
        setupLayers(mantle, water, LiquidType.WATER, stone, sand, dirt, grass, snow, ice);

        // Replace stone with sand on the sea shores
        addChunkDecorator(
                new BeachDecorator(new BlockCollectionFilter(stone), sand, 2, 5));

        Filter<Block> removableBlocks = new BlockCollectionFilter(Arrays.asList(stone, sand, dirt, grass, snow));

        // Dig some caves in the terrain
        addChunkDecorator(
                new CaveDecorator(removableBlocks, new PDist(0.1f, 0f), new PDist(5f, 1f), new PDist(100f, 70f), new PDist(70f, 10f), new PDist(2f, 0.5f)));

        // Setup ore spawning
        setupOreGenerator(stone);

        // Setup flora growing in the world
        setupFlora();
    }

    private void setupFlora() {
        FloraFeatureGenerator floraDecorator = new FloraFeatureGenerator(new PDist(2f, 0.4f), new PDist(20f, 0.6f), new PDist(160f, 40f));

        addFeatureGenerator(floraDecorator);
    }


    private void setupOreGenerator(Block stone) {
        Filter<Block> replacedBlocks = new BlockCollectionFilter(stone);
        OreDecorator oreDecorator = new OreDecorator(replacedBlocks);

        // Use plugin mechanism to setup required ores for the modules, by default WoodAndStone requires no
        // ores

        addChunkDecorator(oreDecorator);
    }

    private void setupLayers(Block mantle, Block sea, LiquidType seaType, Block stone, Block sand, Block dirt, Block grass, Block snow, Block ice) {
        LayeringConfig config = new LayeringConfig(mantle, stone, sea, seaType);

        LayeringDecorator layering = new LayeringDecorator(config);

        DefaultLayersDefinition desertDef = new DefaultLayersDefinition(DesertBiome.ID);
        desertDef.addLayerDefinition(new PDist(3, 1), sand, false);
        desertDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        layering.addBiomeLayers(desertDef);

        DefaultLayersDefinition forestDef = new DefaultLayersDefinition(ForestBiome.ID);
        DefaultLayersDefinition plainsDef = new DefaultLayersDefinition(PlainsBiome.ID);
        forestDef.addLayerDefinition(new PDist(1, 0), grass, false);
        plainsDef.addLayerDefinition(new PDist(1, 0), grass, false);
        forestDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        plainsDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        layering.addBiomeLayers(forestDef);
        layering.addBiomeLayers(plainsDef);

        DefaultLayersDefinition tundraDef = new DefaultLayersDefinition(TundraBiome.ID);
        DefaultLayersDefinition taigaDef = new DefaultLayersDefinition(TaigaBiome.ID);
        tundraDef.addLayerDefinition(new PDist(1, 0), snow, false);
        taigaDef.addLayerDefinition(new PDist(1, 0), snow, false);
        tundraDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        taigaDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        layering.addBiomeLayers(tundraDef);
        layering.addBiomeLayers(taigaDef);

        DefaultLayersDefinition alpineDef = new DefaultLayersDefinition(AlpineBiome.ID);
        alpineDef.addLayerDefinition(new PDist(2f, 1f), ice, false);
        alpineDef.addLayerDefinition(new PDist(1f, 0f), snow, false);
        layering.addBiomeLayers(alpineDef);

        addChunkDecorator(layering);
    }
}
