// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rpgAttributes.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

public abstract class AttributeComponent implements Component {
    @Replicate
    public int value;
}
