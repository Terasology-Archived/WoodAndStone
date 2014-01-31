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
package org.terasology.multiBlock;

import org.terasology.anotherWorld.util.Filter;
import org.terasology.math.Vector3i;

public class BasicHorizontalSizeFilter implements Filter<Vector3i> {
    private int minHorizontal;
    private int maxHorizontal;
    private int minHeight;
    private int maxHeight;

    public BasicHorizontalSizeFilter(int horizontal1Size, int horizontal2Size, int minHeight, int maxHeight) {
        minHorizontal = Math.min(horizontal1Size, horizontal2Size);
        maxHorizontal = Math.max(horizontal1Size, horizontal2Size);
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    @Override
    public boolean accepts(Vector3i value) {
        return minHorizontal == Math.min(value.x, value.z)
                && maxHorizontal == Math.max(value.x, value.z)
                && minHeight <= value.y && maxHeight >= value.y;
    }
}
