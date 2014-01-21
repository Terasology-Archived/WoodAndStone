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
package org.terasology.anotherWorld.coreBiome;

import org.terasology.anotherWorld.Biome;

import javax.vecmath.Vector2f;

public class ForestBiome implements Biome {
    public static final String ID = "AnotherWorld:Forest";
    private Vector2f sweetSpot = new Vector2f(0.5f, 0.5f);

    @Override
    public String getBiomeId() {
        return ID;
    }

    @Override
    public String getBiomeName() {
        return "Forest";
    }

    @Override
    public String getBiomeParent() {
        return null;
    }

    @Override
    public float getRarity() {
        return 1f;
    }

    @Override
    public Vector2f getSweetSpot() {
        return sweetSpot;
    }

    @Override
    public float getFog() {
        return 0.6f;
    }
}
