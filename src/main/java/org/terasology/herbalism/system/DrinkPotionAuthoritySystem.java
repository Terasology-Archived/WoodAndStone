/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.herbalism.system;

import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.assets.management.ContextManager;
import org.terasology.audio.AudioManager;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.HerbEffect;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.PotionCommonEffects;
import org.terasology.herbalism.component.PotionComponent;
import org.terasology.herbalism.effect.DoNothingEffect;
import org.terasology.herbalism.effect.HealEffect;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class DrinkPotionAuthoritySystem extends BaseComponentSystem {
    @In
    private AudioManager audioManager;

    @In
    private GenomeManager genomeManager;

    @In
    private Context context;

    @ReceiveEvent
    public void potionConsumed(ActivateEvent event, EntityRef item, PotionComponent potion, GenomeComponent genome) {
        final HerbEffect effect = genomeManager.getGenomeProperty(item, Herbalism.EFFECT_PROPERTY, HerbEffect.class);
        final float magnitude = genomeManager.getGenomeProperty(item, Herbalism.MAGNITUDE_PROPERTY, Float.class);
        final long duration = genomeManager.getGenomeProperty(item, Herbalism.DURATION_PROPERTY, Long.class);
        effect.applyEffect(item, event.getInstigator(), magnitude, duration);
    }

    // Consume a potion without a Genome attached to it. Usually predefined ones.
    @ReceiveEvent
    public void potionWithoutGenomeConsumed(ActivateEvent event, EntityRef item, PotionComponent potion) {
        PotionComponent p = item.getComponent(PotionComponent.class);

        if (p.effect.equals(PotionCommonEffects.HEAL)) {
            HealEffect effect = new HealEffect();
            effect.applyEffect(item, event.getInstigator(), p.magnitude, p.duration);
        }
        else if (p.effect.equals(PotionCommonEffects.REGEN)) {
            RegenerationAlterationEffect effect = new RegenerationAlterationEffect(context);
            effect.applyEffect(item, event.getInstigator(), p.magnitude, p.duration);
        }
        else if (p.effect.equals(PotionCommonEffects.WALK_SPEED)) {
            WalkSpeedAlterationEffect effect = new WalkSpeedAlterationEffect(context);
            effect.applyEffect(item, event.getInstigator(), p.magnitude, p.duration);
        }
        else if (p.effect.equals(PotionCommonEffects.SWIM_SPEED)) {
            SwimSpeedAlterationEffect effect = new SwimSpeedAlterationEffect(context);
            effect.applyEffect(item, event.getInstigator(), p.magnitude, p.duration);
        }
        else {
            DoNothingEffect effect = new DoNothingEffect();
            effect.applyEffect(item, event.getInstigator(), p.magnitude, p.duration);
        }

        audioManager.playSound(Assets.getSound("engine:drink").get(), 1.0f);
    }
}
