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

import java.util.Collection;

public class BiomeRandomizer {
    private final int randomizerGranularity = 1000;
    private Biome[] biomesLookupTable = new Biome[randomizerGranularity];

    public BiomeRandomizer(Collection<Biome> biomes) {
        float rarityTotal = 0;
        float[] rarities = new float[biomes.size()];
        Biome[] biomesTable = new Biome[biomes.size()];
        int index = 0;
        for (Biome biome : biomes) {
            rarityTotal += biome.getRarity();
            rarities[index] = rarityTotal;
            biomesTable[index] = biome;
            index++;
        }

        final float step = rarityTotal / randomizerGranularity;

        int startIndex = 0;
        for (int i = 0; i < randomizerGranularity; i++) {
            float position = step * i;
            for (int j = startIndex; j < rarities.length; j++) {
                if (position < rarities[j]) {
                    biomesLookupTable[i] = biomesTable[j];
                    startIndex = j;
                    break;
                }
            }
        }
    }

    public Biome getRandomBiome(float randomNumber) {
        if (randomNumber < 0 || randomNumber >= 1) {
            throw new IllegalArgumentException("Can't get a biome for randomNumber outside of expected range 0<=x<1");
        }
        return biomesLookupTable[(int) (randomNumber * randomizerGranularity)];
    }
}
