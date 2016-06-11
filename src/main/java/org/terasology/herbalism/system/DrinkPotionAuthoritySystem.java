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

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedComponent;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.audio.AudioManager;
import org.terasology.context.Context;
import org.terasology.entitySystem.Component;
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
import org.terasology.herbalism.effect.*;
import org.terasology.herbalism.events.BeforeDrinkPotionEvent;
import org.terasology.herbalism.events.DrinkPotionEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class DrinkPotionAuthoritySystem extends BaseComponentSystem {
    @In
    private AudioManager audioManager;

    @In
    private GenomeManager genomeManager;

    @In
    private Context context;

    // TODO: The following two are temporary until moved to AlterationEffects.
    public static final String EXPIRE_TRIGGER_PREFIX = "WoodAndStone:Expire:";
    private Map<String, Class<? extends Component>> effectComponents = new HashMap<>();

    @Override
    public void initialise() {
        effectComponents.put(PotionCommonEffects.JUMP_SPEED, JumpSpeedComponent.class);
    }

    @ReceiveEvent
    public void potionConsumed(ActivateEvent event, EntityRef item, PotionComponent potion, GenomeComponent genome) {

        // TODO: Stopgap fix. If this potion is not supposed to have a dynamically-set Genome, return.
        if (!potion.hasGenome) {
            return;
        }

        final HerbEffect effect = genomeManager.getGenomeProperty(item, Herbalism.EFFECT_PROPERTY, HerbEffect.class);
        final float magnitude = genomeManager.getGenomeProperty(item, Herbalism.MAGNITUDE_PROPERTY, Float.class);
        final long duration = genomeManager.getGenomeProperty(item, Herbalism.DURATION_PROPERTY, Long.class);
        effect.applyEffect(item, event.getInstigator(), magnitude, duration);
    }

    private void checkDrink(EntityRef instigator, EntityRef item, PotionComponent p, HerbEffect h){
        BeforeDrinkPotionEvent beforeDrink = instigator.send(new BeforeDrinkPotionEvent(p, h, instigator, item));

        if (!beforeDrink.isConsumed()) {
            float modifiedMagnitude = beforeDrink.getMagnitudeResultValue();
            long modifiedDuration = (long) beforeDrink.getDurationResultValue();

            if (modifiedMagnitude > 0 && modifiedDuration > 0){
                h.applyEffect(item, instigator, modifiedMagnitude, modifiedDuration);
            }
        }

        audioManager.playSound(Assets.getSound("engine:drink").get(), 1.0f);
    }

    @ReceiveEvent
    public void onPotionWithoutGenomeConsumed(DrinkPotionEvent event, EntityRef ref) {
        PotionComponent p = event.getPotionComponent();
        HerbEffect e = null;

        EntityRef item = event.getItem();

        if (p.effect.equals(PotionCommonEffects.HEAL)) {
            e = new HealEffect();
        } else if (p.effect.equals(PotionCommonEffects.REGEN)) {
            RegenerationAlterationEffect effect = new RegenerationAlterationEffect(context);
            e = new AlterationEffectWrapperHerbEffect(effect, 1f, 1f);
        } else if (p.effect.equals(PotionCommonEffects.WALK_SPEED)) {
            WalkSpeedAlterationEffect effect = new WalkSpeedAlterationEffect(context);
            e = new AlterationEffectWrapperHerbEffect(effect, 1f, 1f);
        } else if (p.effect.equals(PotionCommonEffects.SWIM_SPEED)) {
            SwimSpeedAlterationEffect effect = new SwimSpeedAlterationEffect(context);
            e = new AlterationEffectWrapperHerbEffect(effect, 1f, 1f);
        } else if (p.effect.equals(PotionCommonEffects.JUMP_SPEED)) {
            JumpSpeedAlterationEffect effect = new JumpSpeedAlterationEffect(context);
            e = new AlterationEffectWrapperHerbEffect(effect, 1f, 1f);
        } else {
            e = new DoNothingEffect();
        }

        checkDrink(event.getInstigator(), event.getItem(), p, e);
    }

    // Consume a potion without a Genome attached to it. Usually predefined ones.
    @ReceiveEvent
    public void potionWithoutGenomeConsumed(ActivateEvent event, EntityRef item, PotionComponent potion) {
        PotionComponent p = item.getComponent(PotionComponent.class);
        event.getInstigator().send(new DrinkPotionEvent(p, event.getInstigator(), item));
    }
}
