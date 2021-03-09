/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.heat.component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Side;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.reflection.MappedContainer;

import java.util.List;
import java.util.Set;

@ForceBlockActive
public class HeatProducerComponent implements Component {
    @Replicate
    public List<FuelSourceConsume> fuelConsumed = Lists.newArrayList();
    @Replicate
    public float temperatureAbsorptionRate;
    @Replicate
    public float temperatureLossRate;
    @Replicate
    public float maximumTemperature;
    @Replicate
    public Set<Side> heatDirections = Sets.newHashSet();

    @MappedContainer
    public static class FuelSourceConsume {
        @Replicate
        public long startTime;
        @Replicate
        public float heatProvided;
        @Replicate
        public long burnLength;
    }
}
