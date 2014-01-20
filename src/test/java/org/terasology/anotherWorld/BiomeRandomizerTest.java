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

import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedHashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

public class BiomeRandomizerTest {
    @Test
    public void test() {
        final Biome biome1 = Mockito.mock(Biome.class);
        Mockito.when(biome1.getRarity()).thenReturn(0.75f);
        final Biome biome2 = Mockito.mock(Biome.class);
        Mockito.when(biome2.getRarity()).thenReturn(0.25f);

        Set<Biome> biomes = new LinkedHashSet<>();
        biomes.add(biome1);
        biomes.add(biome2);

        BiomeRandomizer biomeRand = new BiomeRandomizer(biomes);
        assertEquals(biome1, biomeRand.getRandomBiome(0));
        assertEquals(biome1, biomeRand.getRandomBiome(0.6f));
        assertEquals(biome1, biomeRand.getRandomBiome(0.74f));
        assertEquals(biome2, biomeRand.getRandomBiome(0.75f));
        assertEquals(biome2, biomeRand.getRandomBiome(0.9f));
        assertEquals(biome2, biomeRand.getRandomBiome(0.99f));
    }
}
