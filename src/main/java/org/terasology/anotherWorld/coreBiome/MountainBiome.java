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

public class MountainBiome implements Biome {
    @Override
    public String getBiomeId() {
        return "AnotherWorld:Mountain";
    }

    @Override
    public String getBiomeName() {
        return "Mountain";
    }

    @Override
    public String getBiomeParent() {
        return null;
    }

    @Override
    public float getRarity() {
        return 0.4f;
    }

    @Override
    public float getInfluenceStrength() {
        return 0.3f;
    }

    @Override
    public float getDesiredHeight() {
        return 0.8f;
    }

    @Override
    public float getFog() {
        return 0.7f;
    }

    @Override
    public float getTemperature() {
        return 0.2f;
    }

    @Override
    public float getHumidity() {
        return 0.1f;
    }
}
