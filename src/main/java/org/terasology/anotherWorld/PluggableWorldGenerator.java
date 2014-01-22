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
package org.terasology.anotherWorld;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.anotherWorld.coreBiome.DesertBiome;
import org.terasology.anotherWorld.coreBiome.ForestBiome;
import org.terasology.anotherWorld.coreBiome.PlainsBiome;
import org.terasology.anotherWorld.coreBiome.TundraBiome;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.DependencyInfo;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.WorldGenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PluggableWorldGenerator implements WorldGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PluggableWorldGenerator.class);

    private String seed;
    private Map<String, Biome> biomes = new HashMap<>();

    private Vector3i chunkSize = new Vector3i(16, 256, 16);

    private List<ChunkDecorator> chunkDecorators = new LinkedList<>();
    private List<FeatureGenerator> featureGenerators = new LinkedList<>();

    private BiomeProvider biomeProvider;
    private int seaLevel = 32;
    private int maxLevel = 220;

    private LandscapeGenerator landscapeGenerator;
    private SimpleUri uri;

    public PluggableWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    public void setLandscapeGenerator(LandscapeGenerator landscapeGenerator) {
        this.landscapeGenerator = landscapeGenerator;
    }

    public void addChunkDecorator(ChunkDecorator chunkGenerator) {
        chunkDecorators.add(chunkGenerator);
    }

    public void addFeatureGenerator(FeatureGenerator featureGenerator) {
        featureGenerators.add(featureGenerator);
    }

    public void setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    @Override
    public final void initialize() {
    }

    @Override
    public void setWorldSeed(String seed) {
        this.seed = seed;

        initializeCoreBiomes();
//        loadBiomes();

        biomeProvider = new BiomeProvider(seed, biomes, seaLevel, maxLevel);

        appendGenerators();

        landscapeGenerator.initializeWithSeed(seed);

        for (ChunkDecorator chunkDecorator : chunkDecorators) {
            chunkDecorator.initializeWithSeed(seed);
        }

        for (FeatureGenerator featureGenerator : featureGenerators) {
            featureGenerator.initializeWithSeed(seed);
        }
    }

    protected abstract void appendGenerators();

    private void initializeCoreBiomes() {
        Biome desert = new DesertBiome();
        biomes.put(desert.getBiomeId(), desert);
        Biome forest = new ForestBiome();
        biomes.put(forest.getBiomeId(), forest);
        Biome plains = new PlainsBiome();
        biomes.put(plains.getBiomeId(), plains);
        Biome tundra = new TundraBiome();
        biomes.put(tundra.getBiomeId(), tundra);
    }

    private void loadBiomes() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

        for (Module module : moduleManager.getActiveModules()) {
            loadBiomesFromModule(moduleManager, module);
        }
    }

    private void loadBiomesFromModule(ModuleManager moduleManager, Module module) {
        for (DependencyInfo dependency : module.getModuleInfo().getDependencies()) {
            loadBiomesFromModule(moduleManager, moduleManager.getLatestModuleVersion(dependency.getId()));
        }
        if (module.isCodeModule()) {
            loadBiomesImpl(module.getReflections());
        }
    }

    private void loadBiomesImpl(Reflections reflections) {
        final Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(RegisterBiome.class);
        for (Class<?> biomeClass : typesAnnotatedWith) {
            if (!Biome.class.isAssignableFrom(biomeClass)) {
                logger.error("Found a class " + biomeClass.getName() + " that has the @RegisterBiome annotation but does not implement Biome interface");
                continue;
            }
            try {
                final Constructor<?> constructor = biomeClass.getConstructor(new Class[0]);
                final Biome biome = (Biome) constructor.newInstance(new Object[0]);
                biomes.put(biome.getBiomeId(), biome);
            } catch (NoSuchMethodException e) {
                logger.error("Found a class " + biomeClass.getName() + " that has the @RegisterBiome but does not have a default constructor", e);
            } catch (InvocationTargetException e) {
                logger.error("Found a class " + biomeClass.getName() + " that has the @RegisterBiome but can't instantiate it", e);
            } catch (InstantiationException e) {
                logger.error("Found a class " + biomeClass.getName() + " that has the @RegisterBiome but can't instantiate it", e);
            } catch (IllegalAccessException e) {
                logger.error("Found a class " + biomeClass.getName() + " that has the @RegisterBiome but can't instantiate it", e);
            }
        }
    }

    @Override
    public void applySecondPass(Vector3i chunkPos, ChunkView view) {
        for (FeatureGenerator featureGenerator : featureGenerators) {
            featureGenerator.generateInChunk(chunkPos, view, biomeProvider);
        }
    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    @Override
    public void createChunk(Chunk chunk) {
        ChunkInformation chunkInformation = new ChunkInformation();

        landscapeGenerator.generateInChunk(chunk, chunkInformation, seaLevel, maxLevel);

        for (ChunkDecorator chunkDecorator : chunkDecorators) {
            chunkDecorator.generateInChunk(chunk, chunkInformation, biomeProvider, seaLevel);
        }
    }

    @Override
    public float getFog(float x, float y, float z) {
        return biomeProvider.getBaseBiomeAt(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(z + 0.5f)).getFog();
    }

    @Override
    public float getTemperature(float x, float y, float z) {
        return biomeProvider.getTemperature(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public float getHumidity(float x, float y, float z) {
        return biomeProvider.getHumidity(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }
}
