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
import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BiomeProvider {
    private ConditionsBaseProvider conditions;
    private Collection<Biome> biomes;
    private int seeLevel;
    private int maxLevel;
    private Biome defaultBiome;

    public BiomeProvider(String worldSeed, Collection<Biome> biomes, int seeLevel, int maxLevel, Biome defaultBiome) {
        this.biomes = biomes;
        this.seeLevel = seeLevel;
        this.maxLevel = maxLevel;
        this.defaultBiome = defaultBiome;

        conditions = new ConditionsBaseProvider(worldSeed);
    }

    public Biome getBaseBiomeAt(int x, int z) {
        float temperatureBase = conditions.getTemperatureAtSeeLevel(x, z);
        float humidityBase = conditions.getHumidityAtSeeLevel(x, z);
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

        for (Biome biome : biomes) {
            final Vector2f sweetSpot = biome.getSweetSpot();
            Vector2f conditions = new Vector2f(temp, hum);
            conditions.sub(sweetSpot);
            final float rarity = biome.getRarity();
            float priority = conditions.length() * rarity;
            if (priority > maxPriority) {
                chosenBiome = biome;
                maxPriority = priority;
            }
        }
        return chosenBiome;
    }

    public float getTemperature(int x, int y, int z) {
        float temperatureBase = conditions.getTemperatureAtSeeLevel(x, z);
        if (y == seeLevel) {
            return temperatureBase;
        }

        if (y > seeLevel) {
            return temperatureBase / ((y - seeLevel) / maxLevel + 1);
        } else {
            return temperatureBase * seeLevel / (seeLevel - y);
        }
    }

    public float getHumidity(int x, int y, int z) {
        float humidityBase = conditions.getHumidityAtSeeLevel(x, z);
        if (y == seeLevel) {
            return humidityBase;
        }

        if (y > seeLevel) {
            return humidityBase / ((y - seeLevel) / maxLevel + 1);
        } else {
            return humidityBase / ((seeLevel - y) / maxLevel + 1);
        }
    }
}
