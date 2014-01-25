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
package org.terasology.anotherWorld.decorator.structure.provider;

import org.terasology.anotherWorld.decorator.structure.PocketStructureDefinition;
import org.terasology.world.block.Block;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UniformPocketBlockProvider implements PocketStructureDefinition.PocketBlockProvider {
    private Block block;

    public UniformPocketBlockProvider(Block block) {
        this.block = block;
    }

    @Override
    public Block getBlock(float distanceFromCenter) {
        return block;
    }
}
