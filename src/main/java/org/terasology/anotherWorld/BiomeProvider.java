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

import javax.vecmath.Vector2f;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BiomeProvider {
    private ConditionsBaseProvider conditions;
    private Map<String, Biome> biomes;
    private int seaLevel;
    private int maxLevel;

    public BiomeProvider(String worldSeed, Map<String, Biome> biomes, int seaLevel, int maxLevel) {
        this.biomes = biomes;
        this.seaLevel = seaLevel;
        this.maxLevel = maxLevel;

        conditions = new ConditionsBaseProvider(worldSeed);
    }

    public Biome getBiomeById(String biomeId) {
        return biomes.get(biomeId);
    }

    public Biome getBaseBiomeAt(int x, int z) {
        float temperatureBase = conditions.getTemperatureAtseaLevel(x, z);
        float humidityBase = conditions.getHumidityAtseaLevel(x, z);
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
        float temperatureBase = conditions.getTemperatureAtseaLevel(x, z);
        if (y == seaLevel) {
            return temperatureBase;
        }

        if (y > seaLevel) {
            return temperatureBase / ((y - seaLevel) / (maxLevel - seaLevel) + 1);
        } else {
            return temperatureBase * seaLevel / (seaLevel - y);
        }
    }

    public float getHumidity(int x, int y, int z) {
        float humidityBase = conditions.getHumidityAtseaLevel(x, z);
        if (y == seaLevel) {
            return humidityBase;
        }

        if (y > seaLevel) {
            return humidityBase / ((y - seaLevel) / (maxLevel - seaLevel) + 1);
        } else {
            return humidityBase / ((seaLevel - y) / (maxLevel - y) + 1);
        }
    }
}
