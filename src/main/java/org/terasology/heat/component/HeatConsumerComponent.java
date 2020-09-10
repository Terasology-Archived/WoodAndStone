// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.heat.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Side;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.nui.reflection.MappedContainer;

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
