// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism;

import org.terasology.anotherWorld.util.ChanceRandomizer;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;

@RegisterSystem
@Share(HerbEffectRegistry.class)
public class DefaultHerbEffectRegistry extends BaseComponentSystem implements HerbEffectRegistry {
    private final ChanceRandomizer<HerbEffect> herbEffectRandomizer = new ChanceRandomizer<>(1000);

    @Override
    public void postBegin() {
        herbEffectRandomizer.initialize();
    }

    @Override
    public void registerHerbEffect(float rarity, HerbEffect herbEffect) {
        herbEffectRandomizer.addChance(rarity, herbEffect);
    }

    @Override
    public HerbEffect getHerbEffect(float value) {
        return herbEffectRandomizer.getObject(value);
    }
}
