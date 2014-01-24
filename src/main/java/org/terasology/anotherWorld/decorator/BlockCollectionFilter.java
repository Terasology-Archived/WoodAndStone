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
package org.terasology.anotherWorld.decorator;

import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockCollectionFilter implements BlockFilter {
    private Collection<Block> blocks;

    public BlockCollectionFilter(Block block) {
        this(Collections.singleton(block));
    }

    public BlockCollectionFilter(Collection<Block> blocks) {
        this.blocks = blocks;
    }

    @Override
    public boolean accepts(Chunk chunk, int x, int y, int z) {
        if (chunk.getBlock(x, y, z).getURI().getFamilyName().equalsIgnoreCase("snow")) {
            int sdfasdf = 0;
        }
        return blocks.contains(chunk.getBlock(x, y, z));
    }
}
