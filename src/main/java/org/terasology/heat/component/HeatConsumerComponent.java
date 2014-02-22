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
@Replicate
public class HeatConsumerComponent implements Component {
    public Set<Side> heatDirections;
    public float heatConsumptionEfficiency;
    public List<ResidualHeat> residualHeat = Lists.newArrayList();

    @MappedContainer
    public static class ResidualHeat {
        public long time;
        public float baseHeat;
    }
}
