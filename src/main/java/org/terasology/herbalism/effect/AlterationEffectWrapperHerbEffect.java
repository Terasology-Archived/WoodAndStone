// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.effect;

import org.terasology.alterationEffects.AlterationEffect;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.herbalism.HerbEffect;

public class AlterationEffectWrapperHerbEffect implements HerbEffect {
    private final AlterationEffect alterationEffect;
    private final float durationMultiplier;
    private final float magnitudeMultiplier;

    public AlterationEffectWrapperHerbEffect(AlterationEffect alterationEffect, float durationMultiplier,
                                             float magnitudeMultiplier) {
        this.alterationEffect = alterationEffect;
        this.durationMultiplier = durationMultiplier;
        this.magnitudeMultiplier = magnitudeMultiplier;
    }

    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, float magnitude, long duration) {
        alterationEffect.applyEffect(instigator, entity, magnitude * magnitudeMultiplier,
                (long) (duration * durationMultiplier));
    }
}
