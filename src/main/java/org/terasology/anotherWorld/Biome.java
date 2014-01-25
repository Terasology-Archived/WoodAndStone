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

import org.terasology.world.generator.plugin.WorldGeneratorPlugin;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface Biome extends WorldGeneratorPlugin {
    /**
     * A unique identifier for this biome. Used by mods to map their behaviours based on the id.
     *
     * @return
     */
    String getBiomeId();

    /**
     * Returns human readable name of the biome.
     *
     * @return
     */
    String getBiomeName();

    /**
     * What is the closest relative to this biome. Used when a mod requires to know the conditions in the biome,
     * but do not know this biome by it's ID. Only the core biomes can return a null value for this method.
     *
     * @return
     */
    String getBiomeParent();

    /**
     * How rare this biome is, on a scale from 0<rarity<=1.
     *
     * @return
     */
    float getRarity();

    /**
     * Returns the sweet-spot for this biome in terms of: temperature, humidity, terrain and height above sea level.
     * Biome that matches best the specified criteria will be chosen for each block.
     *
     * @return
     */
    SweetSpot getSweetSpot();

    float getFog();

    /**
     * Each value specifies what is the desired value for the field, for this biome to be allocated to a spot in the world.
     * Biome also specifies how important this field is, when determining the best fit. For example, if a biome doesn't really
     * care that much about temperature fitting the desired value, but does care about humidity to be around 0.9 (90%), than
     * it should specify high weight for humidity, and low weight for temperature.
     * All the values below have to be in range of 0<=value<=1.
     * All the weights HAVE TO add up to 1.
     */
    public interface SweetSpot {
        float getHumidity();

        float getHumidityWeight();

        float getTemperature();

        float getTemperatureWeight();

        float getTerrain();

        float getTerrainWeight();

        float getAboveSeaLevel();

        float getAboveSeaLevelWeight();
    }
}
