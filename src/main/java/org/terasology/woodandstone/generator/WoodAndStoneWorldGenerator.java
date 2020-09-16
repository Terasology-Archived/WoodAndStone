// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodandstone.generator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.ChunkDecorator;
import org.terasology.anotherWorld.FeatureGenerator;
import org.terasology.anotherWorld.decorator.BeachDecorator;
import org.terasology.anotherWorld.decorator.BiomeDecorator;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.anotherWorld.decorator.CaveDecorator;
import org.terasology.anotherWorld.decorator.layering.DefaultLayersDefinition;
import org.terasology.anotherWorld.decorator.layering.LayeringConfig;
import org.terasology.anotherWorld.decorator.layering.LayeringDecorator;
import org.terasology.anotherWorld.decorator.ore.OreDecorator;
import org.terasology.anotherWorld.generation.BiomeProvider;
import org.terasology.anotherWorld.generation.HillynessProvider;
import org.terasology.anotherWorld.generation.HumidityProvider;
import org.terasology.anotherWorld.generation.PerlinSurfaceHeightProvider;
import org.terasology.anotherWorld.generation.TemperatureProvider;
import org.terasology.anotherWorld.generation.TerrainVariationProvider;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.anotherWorld.util.Provider;
import org.terasology.anotherWorld.util.alpha.IdentityAlphaFunction;
import org.terasology.anotherWorld.util.alpha.MinMaxAlphaFunction;
import org.terasology.anotherWorld.util.alpha.PowerAlphaFunction;
import org.terasology.anotherWorld.util.alpha.UniformNoiseAlpha;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.climateConditions.ConditionsBaseField;
import org.terasology.coreworlds.generator.facetProviders.SeaLevelProvider;
import org.terasology.coreworlds.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldConfiguratorAdapter;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.growingflora.generator.BushProvider;
import org.terasology.growingflora.generator.FloraFeatureGenerator;
import org.terasology.growingflora.generator.FloraProvider;
import org.terasology.growingflora.generator.FoliageProvider;
import org.terasology.growingflora.generator.TreeProvider;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterWorldGenerator(id = "throughoutTheAges", displayName = "Throughout the Ages", description = "Generates the " +
        "world for playing 'Throughout the Ages' content mods.")
public class WoodAndStoneWorldGenerator extends BaseFacetedWorldGenerator {
    private final List<ChunkDecorator> chunkDecorators = new LinkedList<>();
    private final List<FeatureGenerator> featureGenerators = new LinkedList<>();
    private final List<FacetProvider> facetProviders = new LinkedList<>();
    private BlockManager blockManager;
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;
    private int seaLevel = 32;
    private int maxLevel = 220;
    private float biomeDiversity = 0.5f;

    private Function<Float, Float> temperatureFunction = IdentityAlphaFunction.singleton();
    private Function<Float, Float> humidityFunction = IdentityAlphaFunction.singleton();

    private PerlinSurfaceHeightProvider surfaceHeightProvider;

    public WoodAndStoneWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    public void addChunkDecorator(ChunkDecorator chunkGenerator) {
        chunkDecorators.add(chunkGenerator);
    }

    public void addFeatureGenerator(FeatureGenerator featureGenerator) {
        featureGenerators.add(featureGenerator);
    }

    public void addFacetProvider(FacetProvider facetProvider) {
        facetProviders.add(facetProvider);
    }

    public void setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * 0=changing slowly, 1=changing frequently
     *
     * @param biomeDiversity
     */
    public void setBiomeDiversity(float biomeDiversity) {
        this.biomeDiversity = biomeDiversity;
    }

    public void setTemperatureFunction(Function<Float, Float> temperatureFunction) {
        this.temperatureFunction = temperatureFunction;
    }

    public void setHumidityFunction(Function<Float, Float> humidityFunction) {
        this.humidityFunction = humidityFunction;
    }

    public void setLandscapeOptions(float seaFrequency, float terrainDiversity,
                                    Function<Float, Float> generalTerrainFunction,
                                    Function<Float, Float> heightBelowSeaLevelFunction,
                                    Function<Float, Float> heightAboveSeaLevelFunction,
                                    float hillinessDiversity, Function<Float, Float> hillynessFunction) {
        surfaceHeightProvider = new PerlinSurfaceHeightProvider(seaFrequency, terrainDiversity, generalTerrainFunction,
                heightBelowSeaLevelFunction,
                heightAboveSeaLevelFunction,
                hillinessDiversity, hillynessFunction, seaLevel, maxLevel);
    }

    @Override
    public void initialize() {
        setupRasterizerInitializer();

        getWorld().initialize();
    }

    private void setupRasterizerInitializer() {
        addChunkDecorator(new BiomeDecorator());

        blockManager = CoreRegistry.get(BlockManager.class);

        final Block mantle = blockManager.getBlock("CoreAssets:MantleStone");
        final Block stone = blockManager.getBlock("CoreAssets:Stone");
        final Block water = blockManager.getBlock("CoreAssets:Water");
        final Block sand = blockManager.getBlock("CoreAssets:Sand");
        final Block clay = blockManager.getBlock("WoodAndStone:ClayStone");
        final Block dirt = blockManager.getBlock("CoreAssets:Dirt");
        final Block grass = blockManager.getBlock("CoreAssets:Grass");
        final Block snow = blockManager.getBlock("CoreAssets:Snow");
        final Block ice = blockManager.getBlock("CoreAssets:Ice");

        // Setup biome terrain layers
        setupLayers(mantle, water, stone, sand, dirt, grass, snow, ice, seaLevel);

        // Replace stone with sand on the sea shores
        addChunkDecorator(
                new BeachDecorator(new BlockCollectionPredicate(Arrays.asList(stone, dirt, grass, snow)),
                        new BeachBlockProvider(0.05f, clay, sand), seaLevel - 5, seaLevel + 2));

        Predicate<Block> removableBlocks = new BlockCollectionPredicate(Arrays.asList(stone, sand, dirt, grass, snow));

        // Dig some caves in the terrain
        addChunkDecorator(
                new CaveDecorator(getWorldSeed().hashCode(), removableBlocks, new PDist(0.2f, 0f), new PDist(5f, 1f),
                        new PDist(1750f, 400f), new PDist(50f, 10f), new PDist(2f, 0.5f), blockManager));

        // Setup ore spawning
        setupOreGenerator(stone);

        FloraFeatureGenerator floraDecorator = new FloraFeatureGenerator();
        addFeatureGenerator(floraDecorator);

        if (worldBuilder == null) {
            worldBuilder = createWorld();
        }

        for (ChunkDecorator chunkDecorator : chunkDecorators) {
            worldBuilder.addRasterizer(chunkDecorator);
        }
        for (FeatureGenerator featureGenerator : featureGenerators) {
            worldBuilder.addRasterizer(featureGenerator);
        }
    }

    private void setupGenerator() {
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

        setLandscapeOptions(
                // 40% of the landscape is under water
                0.4f,
                // Semi high continent size
                0.1f,
                // Height is distributed Gaussian
                IdentityAlphaFunction.singleton(),
                // Terrain underwater is more shallow than deep (PowerAlphaFunction) and also at least 0.3*seaLevel 
                // height
                new MinMaxAlphaFunction(new PowerAlphaFunction(IdentityAlphaFunction.singleton(), 0.7f), 0.3f, 1f),
                // Make the lowlands a bit more common than higher areas (using PowerAlphaFunction)
                new PowerAlphaFunction(IdentityAlphaFunction.singleton(), 2f),
                // Smoothen the terrain a bit
                0.5f,
                new MinMaxAlphaFunction(new PowerAlphaFunction(new UniformNoiseAlpha(IdentityAlphaFunction.singleton()), 1.3f), 0.1f, 1f));

        // Setup flora growing in the world
        setupFlora(seaLevel);
    }

    @Override
    protected WorldBuilder createWorld() {
        setupGenerator();

        ClimateConditionsSystem environmentSystem = new ClimateConditionsSystem();
        environmentSystem.setWorldSeed(getWorldSeed());
        environmentSystem.configureHumidity(seaLevel, maxLevel, biomeDiversity, humidityFunction, 0, 1);
        environmentSystem.configureTemperature(seaLevel, maxLevel, biomeDiversity, temperatureFunction, -20, 40);

        ConditionsBaseField temperatureBaseField = environmentSystem.getTemperatureBaseField();
        ConditionsBaseField humidityBaseField = environmentSystem.getHumidityBaseField();

        WorldBuilder worldBuilder = new WorldBuilder(worldGeneratorPluginLibrary);
        worldBuilder.addProvider(new BiomeProvider());
        worldBuilder.addProvider(new HillynessProvider());
        worldBuilder.addProvider(surfaceHeightProvider);
        worldBuilder.addProvider(new SurfaceToDensityProvider());
        worldBuilder.addProvider(new HumidityProvider(humidityBaseField));
        worldBuilder.addProvider(new TemperatureProvider(temperatureBaseField));
        worldBuilder.addProvider(new TerrainVariationProvider());
        worldBuilder.addProvider(new SeaLevelProvider(seaLevel));

        for (FacetProvider facetProvider : facetProviders) {
            worldBuilder.addProvider(facetProvider);
        }

        return worldBuilder;
    }

    @Override
    public WorldConfigurator getConfigurator() {
        return new WorldConfiguratorAdapter();
    }

    private void setupFlora(int seaLevel) {
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
        OreDecorator oreDecorator = new OreDecorator(getWorldSeed().hashCode(), replacedBlocks);

        // Use plugin mechanism to setup required ores for the modules, by default WoodAndStone requires no
        // ores

        addChunkDecorator(oreDecorator);
    }

    private void setupLayers(Block mantle, Block sea, Block stone, Block sand, Block dirt, Block grass, Block snow,
                             Block ice,
                             int seaLevel) {
        LayeringConfig config = new LayeringConfig(mantle, stone, sea);

        LayeringDecorator layering = new LayeringDecorator(config, getWorldSeed().hashCode());

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
        private final float chance;
        private final Block block1;
        private final Block block2;

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
