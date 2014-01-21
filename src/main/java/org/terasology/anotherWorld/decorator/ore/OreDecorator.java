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
package org.terasology.anotherWorld.decorator.ore;

import org.terasology.anotherWorld.BiomeProvider;
import org.terasology.anotherWorld.ChunkDecorator;
import org.terasology.anotherWorld.ChunkInformation;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class OreDecorator implements ChunkDecorator {
    private Map<String, OreDefinition> oreDefinitions = new LinkedHashMap<>();
    private Set<Block> replaceableBlocks;
    private String seed;

    public OreDecorator(Set<Block> replaceableBlocks) {
        this.replaceableBlocks = replaceableBlocks;
    }

    @Override
    public void initializeWithSeed(String seed) {
        this.seed = seed;
        loadOres();
    }

    public void addOreDefinition(String oreId, OreDefinition oreDefinition) {
        oreDefinitions.put(oreId, oreDefinition);
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, BiomeProvider biomeProvider, int seaLevel) {
        Structure.StructureCallback callback = new StructureCallbackImpl(chunk);

        for (OreDefinition oreDefinition : oreDefinitions.values()) {
            Collection<Structure> structures = oreDefinition.generateStructures(chunk, seed, biomeProvider);
            for (Structure structure : structures) {
                structure.generateStructure(callback);
            }
        }
    }

    private void loadOres() {
        // Use reflections to find classes with RegisterLayersDefinition annotation
    }

    private class StructureCallbackImpl implements Structure.StructureCallback {
        private Chunk chunk;

        private StructureCallbackImpl(Chunk chunk) {
            this.chunk = chunk;
        }

        @Override
        public boolean canReplace(int x, int y, int z) {
            boolean validCoords = (x > 0 && y > 1 && z > 0
                    && x < chunk.getChunkSizeX() && y < chunk.getChunkSizeY() && z < chunk.getChunkSizeZ());
            return validCoords && replaceableBlocks.contains(chunk.getBlock(x, y, z));

        }

        @Override
        public void replaceBlock(Vector3i position, float force, Block block) {
            if (canReplace(position.x, position.y, position.z)) {
                chunk.setBlock(position, block);
            }
        }
    }
}
