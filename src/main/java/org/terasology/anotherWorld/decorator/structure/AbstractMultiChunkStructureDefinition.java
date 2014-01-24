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
package org.terasology.anotherWorld.decorator.structure;

import org.terasology.anotherWorld.BiomeProvider;
import org.terasology.anotherWorld.util.ChunkRandom;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.math.Vector3i;
import org.terasology.utilities.random.Random;
import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class AbstractMultiChunkStructureDefinition implements StructureDefinition {
    private PDist frequency;

    protected AbstractMultiChunkStructureDefinition(PDist frequency) {
        this.frequency = frequency;
    }

    @Override
    public final Collection<Structure> generateStructures(Chunk chunk, String seed, BiomeProvider biomeProvider) {
        List<Structure> result = new LinkedList<>();
        float maxRange = getMaxRange();
        Vector3i chunkPosition = chunk.getPos();
        Vector3i chunkSize = new Vector3i(chunk.getChunkSizeX(), chunk.getChunkSizeY(), chunk.getChunkSizeZ());

        int chunksRangeToEvaluate = (int) Math.ceil(Math.max(maxRange / chunk.getChunkSizeX(), maxRange / chunk.getChunkSizeZ()));
        for (int chunkX = -chunksRangeToEvaluate; chunkX <= chunksRangeToEvaluate; chunkX++) {
            for (int chunkZ = -chunksRangeToEvaluate; chunkZ <= chunksRangeToEvaluate; chunkZ++) {
                generateStructuresForChunkWithFrequency(result, seed,
                        new Vector3i(
                                chunkPosition.x + chunkX, 0,
                                chunkPosition.z + chunkZ),
                        chunkSize, chunkX * chunk.getChunkSizeX(), chunkZ * chunk.getChunkSizeZ());
            }
        }

        return result;
    }

    protected final void generateStructuresForChunkWithFrequency(List<Structure> result, String seed, Vector3i chunkPosition, Vector3i chunkSize, int xShift, int zShift) {
        Random random = ChunkRandom.getChunkRandom(seed, chunkPosition, getGeneratorSalt());

        float structuresInChunk = frequency.getValue(random);
        int structuresToGenerateInChunk = (int) structuresInChunk;

        // Check if we "hit" any leftover
        if (random.nextFloat() < structuresInChunk - structuresToGenerateInChunk) {
            structuresToGenerateInChunk++;
        }

        for (int i = 0; i < structuresToGenerateInChunk; i++) {
            generateStructuresForChunk(result, random, chunkSize, xShift, zShift);
        }
    }

    protected abstract float getMaxRange();

    protected abstract int getGeneratorSalt();

    protected abstract void generateStructuresForChunk(List<Structure> result, Random random, Vector3i chunkSize, int xShift, int zShift);
}
