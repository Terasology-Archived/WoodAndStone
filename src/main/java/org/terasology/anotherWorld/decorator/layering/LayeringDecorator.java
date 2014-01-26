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
package org.terasology.anotherWorld.decorator.layering;

import org.terasology.anotherWorld.Biome;
import org.terasology.anotherWorld.BiomeProvider;
import org.terasology.anotherWorld.ChunkDecorator;
import org.terasology.anotherWorld.ChunkInformation;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class LayeringDecorator implements ChunkDecorator {
    private Map<String, LayersDefinition> biomeLayers = new HashMap<>();
    private String seed;

    @Override
    public void initializeWithSeed(String seed) {
        this.seed = seed;
        loadLayers();
    }

    public void addBiomeLayers(LayersDefinition layersDefinition) {
        biomeLayers.put(layersDefinition.getBiomeId(), layersDefinition);
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, int seaLevel, BiomeProvider biomeProvider) {
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                int groundLevel = chunkInformation.getGroundLevel(x, z);
                Biome biome = biomeProvider.getBiomeAt(chunk.getBlockWorldPosX(x), groundLevel, chunk.getBlockWorldPosZ(z));
                LayersDefinition matchingLayers = findMatchingLayers(biomeProvider, biome);
                if (matchingLayers != null) {
                    matchingLayers.generateInChunk(seed, groundLevel, seaLevel, chunk, x, z);
                }
            }
        }
    }

    private LayersDefinition findMatchingLayers(BiomeProvider biomeProvider, Biome biome) {
        LayersDefinition layersDefinition = biomeLayers.get(biome.getBiomeId());
        if (layersDefinition != null) {
            return layersDefinition;
        }
        String biomeParentId = biome.getBiomeParent();
        if (biomeParentId != null) {
            Biome parentBiome = biomeProvider.getBiomeById(biomeParentId);
            if (parentBiome != null) {
                return findMatchingLayers(biomeProvider, parentBiome);
            }
        }
        return null;
    }

    private void loadLayers() {
        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        List<LayersDefinition> loadedLayersDefinitions = pluginLibrary.instantiateAllOfType(LayersDefinition.class);
        for (LayersDefinition layersDefinition : loadedLayersDefinitions) {
            addBiomeLayers(layersDefinition);
        }
    }
}
