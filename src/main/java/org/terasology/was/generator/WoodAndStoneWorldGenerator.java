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
package org.terasology.was.generator;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.PluggableWorldGenerator;
import org.terasology.anotherWorld.decorator.BeachDecorator;
import org.terasology.anotherWorld.decorator.BiomeDecorator;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.anotherWorld.decorator.CaveDecorator;
import org.terasology.anotherWorld.decorator.layering.DefaultLayersDefinition;
import org.terasology.anotherWorld.decorator.layering.LayeringConfig;
import org.terasology.anotherWorld.decorator.layering.LayeringDecorator;
import org.terasology.anotherWorld.decorator.ore.OreDecorator;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.anotherWorld.util.Provider;
import org.terasology.anotherWorld.util.alpha.IdentityAlphaFunction;
import org.terasology.anotherWorld.util.alpha.MinMaxAlphaFunction;
import org.terasology.anotherWorld.util.alpha.PowerAlphaFunction;
import org.terasology.anotherWorld.util.alpha.UniformNoiseAlpha;
import org.terasology.engine.SimpleUri;
import org.terasology.gf.generator.BushProvider;
import org.terasology.gf.generator.FloraFeatureGenerator;
import org.terasology.gf.generator.FloraProvider;
import org.terasology.gf.generator.FoliageProvider;
import org.terasology.gf.generator.TreeProvider;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.liquid.LiquidType;

import java.util.Arrays;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterWorldGenerator(id = "throughoutTheAges", displayName = "Throughout the Ages", description = "Generates the world for playing 'Throughout the Ages' content mods.")
public class WoodAndStoneWorldGenerator extends PluggableWorldGenerator {
    private BlockManager blockManager;

    public WoodAndStoneWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected void setupGenerator() {

        addChunkDecorator(new BiomeDecorator());

        int seaLevel = 1700;
        int maxLevel = 4000;

        setSeaLevel(seaLevel);
        setMaxLevel(maxLevel);

        // Make sure that area on the sea level is not dry, this will prevent deserts spawning next to sea
        setHumidityFunction(
                new MinMaxAlphaFunction(new UniformNoiseAlpha(IdentityAlphaFunction.singleton()), 0.3f, 1f));
        // Make sure that area on the sea level in not too cold, so that colder areas (with snow) will
        // only exist in higher Y-levels
        setTemperatureFunction(
                new MinMaxAlphaFunction(new UniformNoiseAlpha(IdentityAlphaFunction.singleton()), 0.4f, 1f));

        blockManager = CoreRegistry.get(BlockManager.class);

        final Block mantle = blockManager.getBlock("Core:MantleStone");
        final Block stone = blockManager.getBlock("Core:Stone");
        final Block water = blockManager.getBlock("Core:Water");
        final Block sand = blockManager.getBlock("Core:Sand");
        final Block clay = blockManager.getBlock("WoodAndStone:ClayStone");
        final Block dirt = blockManager.getBlock("Core:Dirt");
        final Block grass = blockManager.getBlock("Core:Grass");
        final Block snow = blockManager.getBlock("Core:Snow");
        final Block ice = blockManager.getBlock("Core:Ice");


        setLandscapeOptions(
                // 40% of the landscape is under water
                0.4f,
                // Semi high continent size
                0.1f,
                // Height is distributed Gaussian
                IdentityAlphaFunction.singleton(),
                // Terrain underwater is more shallow than deep (PowerAlphaFunction) and also at least 0.3*seaLevel height
                new MinMaxAlphaFunction(new PowerAlphaFunction(IdentityAlphaFunction.singleton(), 0.7f), 0.3f, 1f),
                // Make the lowlands a bit more common than higher areas (using PowerAlphaFunction)
                new PowerAlphaFunction(IdentityAlphaFunction.singleton(), 2f),
                // Smoothen the terrain a bit
                0.5f, new MinMaxAlphaFunction(new PowerAlphaFunction(new UniformNoiseAlpha(IdentityAlphaFunction.singleton()), 1.3f), 0.1f, 1f));

        // Setup biome terrain layers
        setupLayers(mantle, water, LiquidType.WATER, stone, sand, dirt, grass, snow, ice, seaLevel);

        // Replace stone with sand on the sea shores
        addChunkDecorator(
                new BeachDecorator(new BlockCollectionPredicate(Arrays.asList(stone, dirt, grass, snow)), new BeachBlockProvider(0.05f, clay, sand), seaLevel - 5, seaLevel + 2));

        Predicate<Block> removableBlocks = new BlockCollectionPredicate(Arrays.asList(stone, sand, dirt, grass, snow));

        // Dig some caves in the terrain
        addChunkDecorator(
                new CaveDecorator(getSeed(), removableBlocks, new PDist(0.2f, 0f), new PDist(5f, 1f), new PDist(1750f, 400f), new PDist(50f, 10f), new PDist(2f, 0.5f)));

        // Setup ore spawning
        setupOreGenerator(stone);

        // Setup flora growing in the world
        setupFlora(seaLevel);
    }

    @Override
    public Optional<WorldConfigurator> getConfigurator() {
        return Optional.absent();
    }

    @Override
    public void setConfigurator(WorldConfigurator newConfigurator) {
    }

    private void setupFlora(int seaLevel) {
        FloraFeatureGenerator floraDecorator = new FloraFeatureGenerator();
        addFeatureGenerator(floraDecorator);

        addFacetProvider(new FloraProvider(seaLevel));

        // new PDist(2f, 0.4f)
        addFacetProvider(new TreeProvider(1.2f / (16 * 16)));
        // new PDist(20f, 0.6f)
        addFacetProvider(new BushProvider(10.3f / (16 * 16)));
        // new PDist(160f, 40f)
        addFacetProvider(new FoliageProvider(100f / (16 * 16)));


    }


    private void setupOreGenerator(Block stone) {
        Predicate<Block> replacedBlocks = new BlockCollectionPredicate(stone);
        OreDecorator oreDecorator = new OreDecorator(getSeed(), replacedBlocks);

        // Use plugin mechanism to setup required ores for the modules, by default WoodAndStone requires no
        // ores

        addChunkDecorator(oreDecorator);
    }

    private void setupLayers(Block mantle, Block sea, LiquidType seaType, Block stone, Block sand, Block dirt, Block grass, Block snow, Block ice,
                             int seaLevel) {
        LayeringConfig config = new LayeringConfig(mantle, stone, sea, seaType);

        LayeringDecorator layering = new LayeringDecorator(config, getSeed());

        DefaultLayersDefinition desertDef = new DefaultLayersDefinition(seaLevel, AnotherWorldBiomes.DESERT.getId());
        desertDef.addLayerDefinition(new PDist(3, 1), sand, false);
        desertDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        layering.addBiomeLayers(desertDef);

        DefaultLayersDefinition forestDef = new DefaultLayersDefinition(seaLevel, AnotherWorldBiomes.FOREST.getId());
        DefaultLayersDefinition plainsDef = new DefaultLayersDefinition(seaLevel, AnotherWorldBiomes.PLAINS.getId());
        forestDef.addLayerDefinition(new PDist(1, 0), grass, false);
        plainsDef.addLayerDefinition(new PDist(1, 0), grass, false);
        forestDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        plainsDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        layering.addBiomeLayers(forestDef);
        layering.addBiomeLayers(plainsDef);

        DefaultLayersDefinition tundraDef = new DefaultLayersDefinition(seaLevel, AnotherWorldBiomes.TUNDRA.getId());
        DefaultLayersDefinition taigaDef = new DefaultLayersDefinition(seaLevel, AnotherWorldBiomes.TAIGA.getId());
        tundraDef.addLayerDefinition(new PDist(1, 0), snow, false);
        taigaDef.addLayerDefinition(new PDist(1, 0), snow, false);
        layering.addBiomeLayers(tundraDef);
        layering.addBiomeLayers(taigaDef);

        DefaultLayersDefinition alpineDef = new DefaultLayersDefinition(seaLevel, AnotherWorldBiomes.ALPINE.getId());
        alpineDef.addLayerDefinition(new PDist(2f, 1f), ice, false);
        alpineDef.addLayerDefinition(new PDist(1f, 0f), snow, false);
        layering.addBiomeLayers(alpineDef);

        DefaultLayersDefinition cliffDef = new DefaultLayersDefinition(seaLevel, AnotherWorldBiomes.CLIFF.getId());
        layering.addBiomeLayers(cliffDef);

        addChunkDecorator(layering);
    }

    private final class BeachBlockProvider implements Provider<Block> {
        private float chance;
        private Block block1;
        private Block block2;

        public BeachBlockProvider(float chance, Block block1, Block block2) {
            this.chance = chance;
            this.block1 = block1;
            this.block2 = block2;
        }

        @Override
        public Block provide(float randomValue) {
            return (randomValue < chance) ? block1 : block2;
        }
    }
}
