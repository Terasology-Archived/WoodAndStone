// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.herbalism.component;

import org.terasology.engine.entitySystem.Component;

public class PotionComponent implements Component {
    public String effect;
    public float magnitude;
    public long duration;
    public boolean hasGenome = true; // If a potion has been predefined by a developer, set this to false.
}
