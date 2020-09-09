// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.heat.component;

import org.terasology.engine.entitySystem.Component;


public class HeatProcessedComponent implements Component {
    public float heatRequired;
    public long processingTime;
    public String blockResult;
    public String itemResult;
}
