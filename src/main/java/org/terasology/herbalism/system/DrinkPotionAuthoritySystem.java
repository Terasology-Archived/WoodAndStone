// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedComponent;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.HerbEffect;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.PotionCommonEffects;
import org.terasology.herbalism.component.PotionComponent;
import org.terasology.herbalism.effect.AlterationEffectWrapperHerbEffect;
import org.terasology.herbalism.effect.DoNothingEffect;
import org.terasology.herbalism.effect.HealEffect;
import org.terasology.herbalism.events.BeforeDrinkPotionEvent;
import org.terasology.herbalism.events.DrinkPotionEvent;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class DrinkPotionAuthoritySystem extends BaseComponentSystem {
    // TODO: The following two are temporary until moved to AlterationEffects.
    public static final String EXPIRE_TRIGGER_PREFIX = "WoodAndStone:Expire:";
    private final Map<String, Class<? extends Component>> effectComponents = new HashMap<>();
    @In
    private AudioManager audioManager;
    @In
    private GenomeManager genomeManager;
    @In
    private Context context;

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

    private void checkDrink(EntityRef instigator, EntityRef item, PotionComponent p, HerbEffect h) {
        BeforeDrinkPotionEvent beforeDrink = instigator.send(new BeforeDrinkPotionEvent(p, h, instigator, item));

        if (!beforeDrink.isConsumed()) {
            float modifiedMagnitude = beforeDrink.getMagnitudeResultValue();
            long modifiedDuration = (long) beforeDrink.getDurationResultValue();

            if (modifiedMagnitude > 0 && modifiedDuration > 0) {
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
