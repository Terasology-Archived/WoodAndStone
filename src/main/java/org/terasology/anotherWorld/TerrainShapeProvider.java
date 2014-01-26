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
import org.terasology.utilities.procedural.Noise2D;
import org.terasology.utilities.procedural.SimplexNoise;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class TerrainShapeProvider {
    private final float minMultiplier = 0.0005f;
    private final float maxMultiplier = 0.01f;

    private final Noise2D hillynessNoise;

    private float noiseMultiplier;
    private AlphaFunction terrainFunction;

    public TerrainShapeProvider(String worldSeed, float terrainDiversity, AlphaFunction terrainFunction) {
        this.terrainFunction = terrainFunction;
        hillynessNoise = new SimplexNoise(worldSeed.hashCode() + 872364);
        noiseMultiplier = minMultiplier + (maxMultiplier - minMultiplier) * terrainDiversity;
    }

    public float getHillyness(int x, int z) {
        double result = hillynessNoise.noise(x * noiseMultiplier, z * noiseMultiplier);
        return terrainFunction.execute((float) TeraMath.clamp((result + 1.0f) / 2.0f));
    }
}
