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
package org.terasology.growingFlora;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.anotherWorld.Biome;
import org.terasology.anotherWorld.BiomeProvider;
import org.terasology.anotherWorld.ChunkDecorator;
import org.terasology.anotherWorld.ChunkInformation;
import org.terasology.anotherWorld.util.ChanceRandomizer;
import org.terasology.anotherWorld.util.ChunkRandom;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.utilities.random.Random;
import org.terasology.world.chunks.Chunk;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class FloraDecorator implements ChunkDecorator {
    private String seed;

    private PDist treeTriesPerChunk;
    private PDist bushTriesPerChunk;
    private PDist foliageTriesPerChunk;

    private Multimap<String, FloraDefinition> treeDefinitions = LinkedHashMultimap.create();
    private Map<String, ChanceRandomizer<FloraDefinition>> treeDefinitionsCache = new HashMap<>();

    public FloraDecorator(PDist treeTriesPerChunk, PDist bushTriesPerChunk, PDist foliageTriesPerChunk) {
        this.treeTriesPerChunk = treeTriesPerChunk;
        this.bushTriesPerChunk = bushTriesPerChunk;
        this.foliageTriesPerChunk = foliageTriesPerChunk;
    }

    @Override
    public void initializeWithSeed(String seed) {
        this.seed = seed;
    }

    public void addTreeDefinition(String biomeId, FloraDefinition floraDefinition) {
        treeDefinitions.put(biomeId, floraDefinition);
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, int seaLevel, BiomeProvider biomeProvider) {
        Random random = ChunkRandom.getChunkRandom(seed, chunk.getPos(), 787234);

        // First, generate trees, as these are the rarest ones
        int treeTries = treeTriesPerChunk.getIntValue(random);
        for (int i = 0; i < treeTries; i++) {
            int x = random.nextInt(chunk.getChunkSizeX());
            int z = random.nextInt(chunk.getChunkSizeZ());

            int groundLevel = chunkInformation.getGroundLevel(x, z);

            Biome biome = biomeProvider.getBiomeAt(chunk.getBlockWorldPosX(x), groundLevel, chunk.getBlockWorldPosZ(z));
            ChanceRandomizer<FloraDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeProvider, treeDefinitionsCache, treeDefinitions);
            FloraDefinition treeDefinition = definitionsForBiome.randomizeObject(random);
            if (treeDefinition != null && random.nextFloat() < treeDefinition.getProbability()) {
                treeDefinition.plantSaplingOnGround(chunk, chunkInformation, x, groundLevel, z);
            }
        }

        // Second, generate bushes, as these are a bit more common

        // Third, generate grass and flowers, as these are the most common
    }

    private ChanceRandomizer<FloraDefinition> getDefinitionsForBiome(
            Biome biome, BiomeProvider biomeProvider,
            Map<String, ChanceRandomizer<FloraDefinition>> cache, Multimap<String, FloraDefinition> definitions) {
        ChanceRandomizer<FloraDefinition> result = cache.get(biome.getBiomeId());
        if (result != null) {
            return result;
        }

        result = new ChanceRandomizer<>(100);
        Biome biomeToAdd = biome;
        while (biomeToAdd != null) {
            for (FloraDefinition floraDefinition : definitions.get(biome.getBiomeId())) {
                result.addChance(floraDefinition.getRarity(), floraDefinition);
            }
            biomeToAdd = biomeProvider.getBiomeById(biomeToAdd.getBiomeParent());
        }
        result.initialize();
        treeDefinitionsCache.put(biome.getBiomeId(), result);
        return result;
    }
}
