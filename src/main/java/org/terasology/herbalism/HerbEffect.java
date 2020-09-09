// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface HerbEffect {
    void applyEffect(EntityRef instigator, EntityRef entity, float magnitude, long duration);
}
