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
import org.terasology.anotherWorld.coreBiome.DesertBiome;
import org.terasology.anotherWorld.coreBiome.ForestBiome;
import org.terasology.anotherWorld.coreBiome.PlainsBiome;
import org.terasology.anotherWorld.coreBiome.TundraBiome;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.engine.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;

import java.util.HashMap;
import java.util.Map;

public class LayeringDecorator implements ChunkDecorator {
    private Map<String, LayersDefinition> biomeLayers = new HashMap<>();

    @Override
    public void initializeWithSeed(String seed) {
        registerCoreLayers();
        loadLayers();

        for (LayersDefinition layersDefinition : biomeLayers.values()) {
            layersDefinition.initializeWithSeed(seed);
        }
    }

    private void registerCoreLayers() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        Block sand = blockManager.getBlock("Core:Sand");
        Block dirt = blockManager.getBlock("Core:Dirt");
        Block grass = blockManager.getBlock("Core:Grass");
        Block snow = blockManager.getBlock("Core:Snow");

        DefaultLayersDefinition desertDef = new DefaultLayersDefinition();
        desertDef.addLayerDefinition(new PDist(3, 1), sand, false);
        desertDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        biomeLayers.put(DesertBiome.ID, desertDef);

        DefaultLayersDefinition commonDef = new DefaultLayersDefinition();
        commonDef.addLayerDefinition(new PDist(1, 0), grass, false);
        commonDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        biomeLayers.put(ForestBiome.ID, commonDef);
        biomeLayers.put(PlainsBiome.ID, commonDef);

        DefaultLayersDefinition tundraDef = new DefaultLayersDefinition();
        tundraDef.addLayerDefinition(new PDist(1, 0), snow, false);
        tundraDef.addLayerDefinition(new PDist(4, 2), dirt, true);
        biomeLayers.put(TundraBiome.ID, tundraDef);
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, BiomeProvider biomeProvider, int seeLevel) {
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                int groundLevel = chunkInformation.getGroundLevel(x, z);
                Biome biome = biomeProvider.getBiomeAt(x, groundLevel, z);
                LayersDefinition matchingLayers = findMatchingLayers(biomeProvider, biome);
                if (matchingLayers != null) {
                    matchingLayers.generateInChunk(groundLevel, seeLevel, chunk, x, z);
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
                return findMatchingLayers(biomeProvider, biome);
            }
        }
        return null;
    }

    private void loadLayers() {
        // Use reflections to find classes with RegisterLayersDefinition annotation
    }
}
