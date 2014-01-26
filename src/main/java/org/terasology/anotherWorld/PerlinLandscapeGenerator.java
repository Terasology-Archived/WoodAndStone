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

import org.terasology.anotherWorld.util.AlphaFunction;
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PerlinLandscapeGenerator implements LandscapeGenerator {
    private BrownianNoise2D noise;
    private double noiseScale;

    private float seaFrequency;
    private Block bottomBlock;
    private Block groundBlock;
    private Block seeBlock;
    private LiquidType liquidType;
    private AlphaFunction heightAboveSeaLevelFunction;

    public PerlinLandscapeGenerator(float seaFrequency, Block bottomBlock, Block groundBlock, Block seeBlock, LiquidType liquidType,
                                    AlphaFunction heightAboveSeaLevelFunction) {
        this.seaFrequency = seaFrequency;
        this.bottomBlock = bottomBlock;
        this.groundBlock = groundBlock;
        this.seeBlock = seeBlock;
        this.liquidType = liquidType;
        this.heightAboveSeaLevelFunction = heightAboveSeaLevelFunction;
    }

    @Override
    public void initializeWithSeed(String seed) {
        noise = new BrownianNoise2D(new SimplexNoise(seed.hashCode()), 6);
        noiseScale = noise.getScale();
    }

    @Override
    public void generateInChunk(Chunk chunk, ChunkInformation chunkInformation, TerrainShapeProvider terrainShape, int seaLevel, int maxLevel) {
        int chunkXStart = chunk.getBlockWorldPosX(0);
        int chunkZStart = chunk.getBlockWorldPosZ(0);

        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                float hillyness = terrainShape.getHillyness(chunkXStart + x, chunkZStart + z);
                float noise = getNoiseInWorld(hillyness, chunkXStart + x, chunkZStart + z);
                int height;
                if (noise < seaFrequency) {
                    height = (int) (seaLevel * noise / seaFrequency);
                } else {
                    // Number in range 0<=alpha<1
                    float alphaAboveSeaLevel = (noise - seaFrequency) / (1 - seaFrequency);
                    float resultAlpha = heightAboveSeaLevelFunction.execute(alphaAboveSeaLevel);
                    height = (int) (seaLevel + resultAlpha * (maxLevel - seaLevel));
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

    private float getNoiseInWorld(float hillyness, int worldX, int worldZ) {
        double noise = 0;
        int scanArea = (int) ((1 - hillyness) * 50);
        int divider = 0;
        // Scan and average heights in the circle of blocks with diameter of "scanArea" (based on hillyness)
        for (int x = worldX - scanArea; x <= worldX + scanArea; x++) {
            int zScan = (int) Math.sqrt(scanArea * scanArea - (x - worldX) * (x - worldX));
            for (int z = worldZ - zScan; z <= worldZ + zScan; z++) {
                noise += this.noise.noise(0.004 * x, 0.004 * z) / noiseScale;
                divider++;
            }
        }
        noise /= divider;
        return (float) TeraMath.clamp((noise + 1.0) / 2);
    }
}
