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

public interface Biome {
    /**
     * A unique identifier for this biome. Used by mods to map their behaviours based on the id.
     *
     * @return
     */
    public String getBiomeId();

    /**
     * Returns human readable name of the biome.
     *
     * @return
     */
    public String getBiomeName();

    /**
     * What is the closest relative to this biome. Used when a mod requires to know the conditions in the biome,
     * but do not know this biome by it's ID. Only the core biomes can return a null value for this method.
     *
     * @return
     */
    public String getBiomeParent();

    /**
     * How rare this biome is, on a scale from 0-1.
     *
     * @return
     */
    public float getRarity();

    /**
     * Defines a multiplier for when determining, which biome a block is in, if more than one biome is in influence
     * range of that block.
     *
     * @return
     */
    public float getInfluenceStrength();

    /**
     * Defines desired height for blocks in the biome. In range of -1<=x<1, where 0 is see level
     *
     * @return
     */
    public float getDesiredHeight();

    /**
     * Defines a fog typical for this biome.
     *
     * @return
     */
    public float getFog();

    /**
     * Defines a temperature typical for this biome.
     *
     * @return
     */
    public float getTemperature();

    /**
     * Defines humidity typical for this biome.
     *
     * @return
     */
    public float getHumidity();
}
