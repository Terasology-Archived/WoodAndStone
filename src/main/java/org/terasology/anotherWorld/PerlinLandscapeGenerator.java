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

    private float seaFrequency;
    private int maxGeneratedHeight;
    private Block bottomBlock;
    private Block groundBlock;
    private Block seeBlock;
    private LiquidType liquidType;

    public PerlinLandscapeGenerator(float seaFrequency, int maxGeneratedHeight,
                                    Block bottomBlock, Block groundBlock, Block seeBlock, LiquidType liquidType) {
        this.seaFrequency = seaFrequency;
        this.maxGeneratedHeight = maxGeneratedHeight;
        this.bottomBlock = bottomBlock;
        this.groundBlock = groundBlock;
        this.seeBlock = seeBlock;
        this.liquidType = liquidType;
    }

    @Override
    public void initializeWithSeed(String seed) {
        noise = new BrownianNoise2D(new SimplexNoise(seed.hashCode()), 6);
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, int seaLevel) {
        int chunkXStart = chunk.getBlockWorldPosX(0);
        int chunkZStart = chunk.getBlockWorldPosZ(0);

        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                double baseNoise = this.noise.noise(0.0008 * (chunkXStart + x), 0.0008 * (chunkZStart + z));
                float noise = (float) TeraMath.clamp((baseNoise + 1.0) / 2.4);
                int height;
                if (noise < seaFrequency) {
                    height = (int) (seaLevel * noise / seaFrequency);
                } else {
                    // Number in range 0<=alpha<1
                    float alphaAboveSeaLevel = (noise - seaFrequency) / (1 - seaFrequency);
                    float resultAlpha = interpretAlpha(alphaAboveSeaLevel);
                    height = (int) (seaLevel + resultAlpha * (maxGeneratedHeight - seaLevel));
                }

                height = Math.min(chunk.getChunkSizeY() - 1, height);

                chunkInformation.setPositionGroundLevel(x, z, height);

                chunk.setBlock(x, 0, z, bottomBlock);

                for (int y = 1; y <= height; y++) {
                    chunk.setBlock(x, y, z, groundBlock);
                }

                for (int y = height + 1; y <= seaLevel; y++) {
                    chunk.setBlock(x, y, z, seeBlock);
                    chunk.setLiquid(x, y, z, new LiquidData(liquidType, LiquidData.MAX_LIQUID_DEPTH));
                }
            }
        }
    }

    private float interpretAlpha(float alphaAboveseaLevel) {
        return (float) Math.pow(alphaAboveseaLevel, 2);
    }
}
