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

import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.FastRandom;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BiomeInfluenceProvider {
    private static final int salt = 31537;

    private Vector3i chunkSize;
    private String seed;
    private Biome defaultBiome;
    private float biomeScarcity;
    private int biomeInfluenceRange;

    private BiomeRandomizer biomeRandomizer;

    public BiomeInfluenceProvider(String seed, Collection<Biome> biomes, Biome defaultBiome,
                                  float biomeScarcity, int biomeInfluenceRange,
                                  Vector3i chunkSize) {
        this.seed = seed;
        this.defaultBiome = defaultBiome;
        this.biomeScarcity = biomeScarcity;
        this.biomeInfluenceRange = biomeInfluenceRange;
        this.chunkSize = chunkSize;

        this.biomeRandomizer = new BiomeRandomizer(biomes);
    }

    private long getSeedForChunk(Vector3i position, int salt) {
        return seed.hashCode() + salt * (31 * position.x + position.y);
    }

    public Map<Biome, Float> getBiomeInfluence(int x, int z) {
        Vector3i chunkPosition = new Vector3i((int) Math.floor(x * 1f / chunkSize.x), 0, (int) Math.floor(z * 1f / chunkSize.y));
        final Map<Vector2i, Biome> biomesInVicinityOfChunk = getBiomesInVicinityOfChunk(chunkPosition);

        return getBiomeInfluenceWithChunkInformation(biomesInVicinityOfChunk, x, z);
    }

    public Map<Biome, Float> getBiomeInfluenceWithChunkInformation(Map<Vector2i, Biome> biomesInVicinity, int x, int z) {
        Map<Biome, Float> result = new HashMap<>();
        for (Map.Entry<Vector2i, Biome> biomeEntry : biomesInVicinity.entrySet()) {
            float distance = distance(biomeEntry.getKey(), x, z);
            if (distance < biomeInfluenceRange) {
                final Biome biome = biomeEntry.getValue();
                final Float existingInfluence = result.get(biome);
                if (existingInfluence != null) {
                    result.put(biome, existingInfluence + distance * biome.getInfluenceStrength());
                } else {
                    result.put(biome, distance * biome.getInfluenceStrength());
                }
            }
        }

        if (result.isEmpty()) {
            result.put(defaultBiome, 1f);
        }

        return result;
    }

    private float distance(Vector2i origin, int x, int z) {
        return (float) Math.sqrt((origin.x - x) * (origin.x - x) + (origin.y - z) * (origin.y - z));
    }

    private BiomeCenterPosition getBiomeForChunk(Vector3i chunkPosition) {
        FastRandom random = new FastRandom(getSeedForChunk(new Vector3i(chunkSize.x, 0, chunkSize.z), salt));
        final boolean hasBiome = random.nextFloat() < biomeScarcity;
        if (hasBiome) {
            BiomeCenterPosition biomeCenterPosition = new BiomeCenterPosition();
            biomeCenterPosition.biome = biomeRandomizer.getRandomBiome(random.nextFloat());
            biomeCenterPosition.location =
                    new Vector2i(chunkPosition.x * chunkSize.x + random.nextInt(chunkSize.x),
                            chunkPosition.z * chunkSize.z + random.nextInt(chunkSize.z));
        }
        return null;
    }

    public Map<Vector2i, Biome> getBiomesInVicinityOfChunk(Vector3i chunkPosition) {
        int xRange = (int) Math.ceil(biomeInfluenceRange * 1f / chunkSize.x);
        int zRange = (int) Math.ceil(biomeInfluenceRange * 1f / chunkSize.z);

        Map<Vector2i, Biome> result = new HashMap();
        for (int x = chunkPosition.x - xRange; x <= chunkPosition.x + xRange; x++) {
            for (int z = chunkPosition.z - zRange; z <= chunkPosition.z + zRange; z++) {
                final BiomeCenterPosition biomeForChunk = getBiomeForChunk(chunkPosition);
                if (biomeForChunk != null) {
                    result.put(biomeForChunk.location, biomeForChunk.biome);
                }
            }
        }
        return result;
    }

    private static class BiomeCenterPosition {
        private Biome biome;
        private Vector2i location;
    }
}
