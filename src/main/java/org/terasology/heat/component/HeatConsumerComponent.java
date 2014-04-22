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
import org.terasology.entitySystem.Component;
import org.terasology.math.Side;
import org.terasology.network.Replicate;
import org.terasology.reflection.MappedContainer;
import org.terasology.world.block.ForceBlockActive;

import java.util.List;
import java.util.Set;

@ForceBlockActive
public class HeatConsumerComponent implements Component {
    @Replicate
    public Set<Side> heatDirections;
    @Replicate
    public float heatConsumptionEfficiency;
    @Replicate
    public List<ResidualHeat> residualHeat = Lists.newArrayList();

    @MappedContainer
    public static class ResidualHeat {
        @Replicate
        public long time;
        @Replicate
        public float baseHeat;
    }
}
