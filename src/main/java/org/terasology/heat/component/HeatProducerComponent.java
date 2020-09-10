// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.heat.component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Side;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.nui.reflection.MappedContainer;

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
