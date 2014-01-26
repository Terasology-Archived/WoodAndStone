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

import org.terasology.anotherWorld.ChunkInformation;
import org.terasology.anotherWorld.decorator.BlockFilter;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockFloraDefinition implements FloraDefinition {
    private float rarity;
    private float probability;
    private Block block;
    private BlockFilter groundFilter;

    public BlockFloraDefinition(float rarity, float probability, Block block, BlockFilter groundFilter) {
        this.rarity = rarity;
        this.probability = probability;
        this.block = block;
        this.groundFilter = groundFilter;
    }

    @Override
    public float getRarity() {
        return rarity;
    }

    @Override
    public float getProbability() {
        return probability;
    }

    @Override
    public void plantSaplingOnGround(Chunk chunk, ChunkInformation chunkInformation, int x, int y, int z) {
        if (groundFilter.accepts(chunk, chunkInformation, x, y, z)) {
            chunk.setBlock(x, y + 1, z, block);
        }
    }
}
