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
package org.terasology.anotherWorld.coreBiome;

import org.terasology.anotherWorld.Biome;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class DefaultSweetSpot implements Biome.SweetSpot {
    private float humidity;
    private float humidityWeight;
    private float temperature;
    private float temperatureWeight;
    private float terrain;
    private float terrainWeight;
    private float aboveSeaLevel;
    private float aboveSeaLevelWeight;

    public DefaultSweetSpot(float humidity, float humidityWeight, float temperature, float temperatureWeight,
                            float terrain, float terrainWeight, float aboveSeaLevel, float aboveSeaLevelWeight) {
        this.humidity = humidity;
        this.humidityWeight = humidityWeight;
        this.temperature = temperature;
        this.temperatureWeight = temperatureWeight;
        this.terrain = terrain;
        this.terrainWeight = terrainWeight;
        this.aboveSeaLevel = aboveSeaLevel;
        this.aboveSeaLevelWeight = aboveSeaLevelWeight;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getHumidityWeight() {
        return humidityWeight;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getTemperatureWeight() {
        return temperatureWeight;
    }

    public float getTerrain() {
        return terrain;
    }

    public float getTerrainWeight() {
        return terrainWeight;
    }

    public float getAboveSeaLevel() {
        return aboveSeaLevel;
    }

    public float getAboveSeaLevelWeight() {
        return aboveSeaLevelWeight;
    }
}
