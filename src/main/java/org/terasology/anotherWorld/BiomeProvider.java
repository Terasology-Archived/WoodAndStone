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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.anotherWorld.coreBiome.DesertBiome;
import org.terasology.anotherWorld.coreBiome.ForestBiome;
import org.terasology.anotherWorld.coreBiome.PlainsBiome;
import org.terasology.anotherWorld.coreBiome.TaigaBiome;
import org.terasology.anotherWorld.coreBiome.TundraBiome;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BiomeProvider {
    private static final Logger logger = LoggerFactory.getLogger(BiomeProvider.class);

    private ConditionsBaseProvider conditions;
    private TerrainShapeProvider terrainShape;
    private Map<String, Biome> biomes = new HashMap<>();

    private int seaLevel;
    private int maxLevel;

    public BiomeProvider(String worldSeed, int seaLevel, int maxLevel, float biomeSize, float terrainDiversity) {
        this.seaLevel = seaLevel;
        this.maxLevel = maxLevel;

        conditions = new ConditionsBaseProvider(worldSeed, biomeSize, 0.3f, 1f, 0.3f, 1f);
        terrainShape = new TerrainShapeProvider(worldSeed, terrainDiversity);

        initializeCoreBiomes();
        loadBiomes();
    }

    public TerrainShapeProvider getTerrainShape() {
        return terrainShape;
    }

    private void initializeCoreBiomes() {
        Biome desert = new DesertBiome();
        biomes.put(desert.getBiomeId(), desert);
        Biome forest = new ForestBiome();
        biomes.put(forest.getBiomeId(), forest);
        Biome plains = new PlainsBiome();
        biomes.put(plains.getBiomeId(), plains);
        Biome tundra = new TundraBiome();
        biomes.put(tundra.getBiomeId(), tundra);
        Biome taiga = new TaigaBiome();
        biomes.put(taiga.getBiomeId(), taiga);
    }

    private void loadBiomes() {
        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        List<Biome> loadedBiomes = pluginLibrary.instantiateAllOfType(Biome.class);
        for (Biome biome : loadedBiomes) {
            biomes.put(biome.getBiomeId(), biome);
        }
    }

    public Biome getBiomeById(String biomeId) {
        return biomes.get(biomeId);
    }

    public Biome getBaseBiomeAt(int x, int z) {
        float temperatureBase = conditions.getBaseTemperature(x, z);
        float humidityBase = conditions.getBaseHumidity(x, z);

        return getBestBiomeMatch(temperatureBase, humidityBase);
    }

    public Biome getBiomeAt(int x, int y, int z) {
        float temp = getTemperature(x, y, z);
        float hum = getHumidity(x, y, z);

        return getBestBiomeMatch(temp, hum);
    }

    private Biome getBestBiomeMatch(float temp, float hum) {
        Biome chosenBiome = null;
        float maxPriority = 0;

        for (Biome biome : biomes.values()) {
            final Vector2f sweetSpot = biome.getSweetSpot();
            Vector2f conditions = new Vector2f(temp, hum);
            conditions.sub(sweetSpot);
            final float rarity = biome.getRarity();
            float matchStrength = conditions.length();
            if (matchStrength == 0) {
                // Exact match
                return biome;
            }

            float priority = rarity / matchStrength;
            if (priority > maxPriority) {
                chosenBiome = biome;
                maxPriority = priority;
            }
        }
        return chosenBiome;
    }

    public float getTemperature(int x, int y, int z) {
        float temperatureBase = conditions.getBaseTemperature(x, z);
        if (y <= seaLevel) {
            return temperatureBase;
        }

        if (y >= maxLevel) {
            return 0;
        }
        // The higher above see level - the colder
        return temperatureBase * (1f * (maxLevel - y) / (maxLevel - seaLevel));
    }

    public float getHumidity(int x, int y, int z) {
        float humidityBase = conditions.getBaseHumidity(x, z);
        if (y <= seaLevel) {
            return humidityBase;
        }

        if (y >= maxLevel) {
            return 0;
        }
        // The higher above see level - the less humid
        return humidityBase * (1f * (maxLevel - y) / (maxLevel - seaLevel));
    }
}
