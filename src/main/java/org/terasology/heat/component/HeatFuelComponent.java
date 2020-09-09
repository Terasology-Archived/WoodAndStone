// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.heat.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.items.AddToBlockBasedItem;

@AddToBlockBasedItem
public class HeatFuelComponent implements Component {
    public float heatProvided;
    public long consumeTime;
}
