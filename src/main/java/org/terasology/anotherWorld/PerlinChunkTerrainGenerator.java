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

import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.liquid.LiquidData;

import java.util.Map;

public class PerlinChunkTerrainGenerator implements ChunkGenerator {
    private BiomeInfluenceProvider biomeInfluenceProvider;
    private Block bottomBlock;
    private Block groundBlock;
    private int seeLevel;
    private LiquidData seeBlock;

    private BrownianNoise3D pGen1;

    public PerlinChunkTerrainGenerator(BiomeInfluenceProvider biomeInfluenceProvider, Block bottomBlock, Block groundBlock, int seeLevel, LiquidData seeBlock) {
        this.biomeInfluenceProvider = biomeInfluenceProvider;
        this.bottomBlock = bottomBlock;
        this.groundBlock = groundBlock;
        this.seeLevel = seeLevel;
        this.seeBlock = seeBlock;
    }

    @Override
    public void initializeWithSeed(String seed) {
        pGen1 = new BrownianNoise3D(new PerlinNoise(seed.hashCode()), 8);
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation) {
        final Map<Vector2i, Biome> biomesInVicinityOfChunk = biomeInfluenceProvider.getBiomesInVicinityOfChunk(chunk.getPos());
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                chunk.setBlock(x, 0, z, bottomBlock);

                final Map<Biome, Float> biomeInfluences = biomeInfluenceProvider.getBiomeInfluenceWithChunkInformation(biomesInVicinityOfChunk, chunk.getBlockWorldPosX(x), chunk.getBlockWorldPosZ(z));
                float heightSum = 0;
                for (Map.Entry<Biome, Float> biomeInfluence : biomeInfluences.entrySet()) {
                    heightSum += biomeInfluence.getKey().getDesiredHeight() * biomeInfluence.getValue();
                }
                float heightAverage = (heightSum / biomeInfluences.size()) * (float) TeraMath.clamp((pGen1.noise(0.004 * x, 0, 0.004 * z) + 1.0) / 2.0);
                if (heightAverage < 0) {
                    final int height = (int) ((1 + heightAverage) * seeLevel);
                    for (int y = 1; y < height; y++) {
                        chunk.setBlock(x, y, z, groundBlock);
                    }
                    chunkInformation.setPositionGroundLevel(x, z, height - 1);
                    for (int y = height; y <= seeLevel; y++) {
                        chunk.setLiquid(x, y, z, seeBlock);
                    }
                } else {
                    final int height = seeLevel + (int) ((chunk.getChunkSizeY() - seeLevel) * heightAverage);
                    for (int y = 1; y < height; y++) {
                        chunk.setBlock(x, y, z, groundBlock);
                    }
                    chunkInformation.setPositionGroundLevel(x, z, height - 1);
                }
            }
        }
    }
}
