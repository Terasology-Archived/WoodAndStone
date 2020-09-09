// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.effect;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.health.logic.event.DoRestoreEvent;
import org.terasology.herbalism.HerbEffect;
import org.terasology.math.TeraMath;

public class HealEffect implements HerbEffect {
    private final int maxHeal = 100;

    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, float magnitude, long duration) {
        entity.send(new DoRestoreEvent(TeraMath.floorToInt(maxHeal * magnitude), instigator));
    }
}
