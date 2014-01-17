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
package org.terasology.workstation.system.recipe;

import org.terasology.entitySystem.entity.EntityRef;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface CraftProcessDisplay {
    /**
     * Returns components and their count that will be used in crafting this recipe.
     *
     * @return
     */
    public Map<Integer, Integer> getComponentSlotAndCount();

    /**
     * Returns an item entity that will be used to display a result of crafting the recipe once.
     *
     * @return
     */
    public EntityRef createResultItemEntityForDisplayOne();

    /**
     * Returns an item entity that will be used to display a result of creating the recipe as many times
     * as possible at the moment (taking into account available components and tools, space in result slot).
     *
     * @return
     */
    public EntityRef getResultItemEntityForDisplayMax();
}
