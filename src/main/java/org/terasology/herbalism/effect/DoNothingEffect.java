// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.effect;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.herbalism.HerbEffect;

public class DoNothingEffect implements HerbEffect {
    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, float magnitude, long duration) {
    }
}
