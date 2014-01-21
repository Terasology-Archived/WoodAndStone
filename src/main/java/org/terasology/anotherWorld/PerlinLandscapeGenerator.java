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
import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

public class PerlinLandscapeGenerator implements LandscapeGenerator {
    private BrownianNoise2D noise;

    private float seeFrequency;
    private Block bottomBlock;
    private Block groundBlock;
    private Block seeBlock;
    private LiquidType liquidType;

    public PerlinLandscapeGenerator(float seeFrequency, Block bottomBlock, Block groundBlock, Block seeBlock, LiquidType liquidType) {
        this.seeFrequency = seeFrequency;
        this.bottomBlock = bottomBlock;
        this.groundBlock = groundBlock;
        this.seeBlock = seeBlock;
        this.liquidType = liquidType;
    }

    @Override
    public void initializeWithSeed(String seed) {
        noise = new BrownianNoise2D(new SimplexNoise(seed.hashCode()), 8);
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, int seeLevel) {
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                float density = (float) TeraMath.clamp((noise.noise(0.004 * chunk.getBlockWorldPosX(x), 0.004 * chunk.getBlockWorldPosZ(z)) + 1.0) / 2.0);
                int height;
                if (density < seeFrequency) {
                    height = (int) (seeLevel * density / seeFrequency);
                } else {
                    height = (int) (seeLevel + (density - seeFrequency) / (1 - seeFrequency) * (chunk.getChunkSizeY() - seeLevel));
                }

                height = Math.min(chunk.getChunkSizeY() - 1, height);

                chunkInformation.setPositionGroundLevel(x, height, z);

                chunk.setBlock(x, 0, z, bottomBlock);

                for (int y = 1; y <= height; y++) {
                    chunk.setBlock(x, y, z, groundBlock);
                }

                for (int y = height + 1; y <= seeLevel; y++) {
                    chunk.setBlock(x, y, z, seeBlock);
                    chunk.setLiquid(x, y, z, new LiquidData(liquidType, LiquidData.MAX_LIQUID_DEPTH));
                }
            }
        }
    }
}
