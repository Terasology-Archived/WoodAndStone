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
import org.terasology.anotherWorld.coreBiome.AlpineBiome;
import org.terasology.anotherWorld.coreBiome.DesertBiome;
import org.terasology.anotherWorld.coreBiome.ForestBiome;
import org.terasology.anotherWorld.coreBiome.PlainsBiome;
import org.terasology.anotherWorld.coreBiome.TaigaBiome;
import org.terasology.anotherWorld.coreBiome.TundraBiome;
import org.terasology.anotherWorld.util.AlphaFunction;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

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

    public BiomeProvider(String worldSeed, int seaLevel, int maxLevel,
                         float biomeSize, AlphaFunction temperatureFunction, AlphaFunction humidityFunction,
                         float terrainDiversity, AlphaFunction terrainFunction) {
        this.seaLevel = seaLevel;
        this.maxLevel = maxLevel;

        conditions = new ConditionsBaseProvider(worldSeed, biomeSize, temperatureFunction, humidityFunction);
        terrainShape = new TerrainShapeProvider(worldSeed, terrainDiversity, terrainFunction);

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
        Biome alpine = new AlpineBiome();
        biomes.put(alpine.getBiomeId(), alpine);
    }

    private void loadBiomes() {
        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        List<Biome> loadedBiomes = pluginLibrary.instantiateAllOfType(Biome.class);
        for (Biome biome : loadedBiomes) {
            try {
                validateBiome(biome);
                biomes.put(biome.getBiomeId(), biome);
            } catch (IllegalArgumentException exp) {
                logger.error("Biome has invalid definition of a sweet-spot");
            }
        }
    }

    private void validateBiome(Biome biome) {
        Biome.SweetSpot sweetSpot = biome.getSweetSpot();
        validateValue(sweetSpot.getAboveSeaLevel());
        validateValue(sweetSpot.getAboveSeaLevelWeight());
        validateValue(sweetSpot.getHumidity());
        validateValue(sweetSpot.getHumidityWeight());
        validateValue(sweetSpot.getTemperature());
        validateValue(sweetSpot.getTemperatureWeight());
        validateValue(sweetSpot.getTerrain());
        validateValue(sweetSpot.getTerrainWeight());

        float weightTotal = sweetSpot.getAboveSeaLevelWeight() + sweetSpot.getHumidityWeight()
                + sweetSpot.getTemperatureWeight() + sweetSpot.getTerrainWeight();

        if (weightTotal > 1.0001 || weightTotal < 0.0009) {
            throw new IllegalArgumentException();
        }
    }

    private void validateValue(float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException();
        }
    }

    public Biome getBiomeById(String biomeId) {
        return biomes.get(biomeId);
    }

    public Biome getBiomeAt(int x, int y, int z) {
        float temp = getTemperature(x, y, z);
        float hum = getHumidity(x, y, z);
        float terrain = terrainShape.getHillyness(x, z);

        return getBestBiomeMatch(temp, hum, terrain, y);
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

    private Biome getBestBiomeMatch(float temp, float hum, float terrain, int yLevel) {
        float height;
        if (yLevel <= seaLevel) {
            height = 0f;
        } else {
            height = (float) TeraMath.clamp(1f * (yLevel - seaLevel) / (maxLevel - seaLevel));
        }

        Biome chosenBiome = null;
        float maxPriority = 0;

        for (Biome biome : biomes.values()) {
            final Biome.SweetSpot sweetSpot = biome.getSweetSpot();
            float matchPriority = 0;

            matchPriority += sweetSpot.getAboveSeaLevelWeight() * (1 - Math.abs(sweetSpot.getAboveSeaLevel() - height));
            matchPriority += sweetSpot.getHumidityWeight() * (1 - Math.abs(sweetSpot.getHumidity() - hum));
            matchPriority += sweetSpot.getTemperatureWeight() * (1 - Math.abs(sweetSpot.getTemperature() - temp));
            matchPriority += sweetSpot.getTerrainWeight() * (1 - Math.abs(sweetSpot.getTerrain() - terrain));

            matchPriority *= biome.getRarity();

            if (matchPriority > maxPriority) {
                chosenBiome = biome;
                maxPriority = matchPriority;
            }
        }
        return chosenBiome;
    }
}
