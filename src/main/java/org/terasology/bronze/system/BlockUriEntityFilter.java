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
package org.terasology.bronze.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.multiBlock.EntityFilter;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockUri;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockUriEntityFilter implements EntityFilter {
    private BlockUri blockUri;

    public BlockUriEntityFilter(BlockUri blockUri) {
        this.blockUri = blockUri;
    }

    @Override
    public boolean accepts(EntityRef entity) {
        BlockComponent component = entity.getComponent(BlockComponent.class);
        return component != null && component.getBlock().getURI().equals(blockUri);
    }
}
