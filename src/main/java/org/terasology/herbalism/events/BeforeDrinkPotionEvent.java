// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.herbalism.events;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ConsumableEvent;
import org.terasology.herbalism.HerbEffect;
import org.terasology.herbalism.component.PotionComponent;

// TODO: Add extends AbstractValueModifiableEvent?
public class BeforeDrinkPotionEvent implements ConsumableEvent {

    private final PotionComponent potion;
    private final HerbEffect effect;
    private final EntityRef instigator;
    private final TFloatList magnitudeMultipliers = new TFloatArrayList();
    private final TFloatList durationMultipliers = new TFloatArrayList();
    private final TFloatList magnitudeModifiers = new TFloatArrayList();
    private final TFloatList durationModifiers = new TFloatArrayList();
    private EntityRef item;
    private boolean consumed;

    //private TFloatList modifiers = new TFloatArrayList(); // Premodifiers specifically.

    public BeforeDrinkPotionEvent(PotionComponent p) {
        potion = p;
        effect = null;
        instigator = EntityRef.NULL;
    }

    public BeforeDrinkPotionEvent(PotionComponent p, HerbEffect h) {
        potion = p;
        effect = h;
        instigator = EntityRef.NULL;
    }

    public BeforeDrinkPotionEvent(PotionComponent p, HerbEffect h, EntityRef instigatorRef) {
        potion = p;
        effect = h;
        instigator = instigatorRef;
    }

    public BeforeDrinkPotionEvent(PotionComponent p, HerbEffect h, EntityRef instigatorRef, EntityRef itemRef) {
        potion = p;
        effect = h;
        instigator = instigatorRef;
        item = itemRef;
    }

    public PotionComponent getBasePotion() {
        return potion;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getItem() {
        return item;
    }

    public TFloatList getMagnitudeMultipliers() {
        return magnitudeMultipliers;
    }

    public TFloatList getDurationMultipliers() {
        return durationMultipliers;
    }

    public TFloatList getMagnitudeModifiers() {
        return magnitudeModifiers;
    }

    public TFloatList getDurationModifiers() {
        return durationModifiers;
    }

    public void multiplyMagnitude(float amount) {
        magnitudeMultipliers.add(amount);
    }

    public void multiplyDuration(float amount) {
        durationMultipliers.add(amount);
    }

    public void addMagnitude(float amount) {
        magnitudeModifiers.add(amount);
    }

    public void addDuration(float amount) {
        durationModifiers.add(amount);
    }

    public void subtractMagnitude(int amount) {
        magnitudeModifiers.add(-amount);
    }

    public void subtractDuration(int amount) {
        durationModifiers.add(-amount);
    }

    public float getMagnitudeResultValue() {
        // For now, add all modifiers and multiply by all multipliers. Negative modifiers cap to zero, but negative
        // multipliers remain.

        float result = potion.magnitude;
        TFloatIterator modifierIter = magnitudeModifiers.iterator();
        while (modifierIter.hasNext()) {
            result += modifierIter.next();
        }
        result = Math.max(0, result);

        TFloatIterator multiplierIter = magnitudeMultipliers.iterator();
        while (multiplierIter.hasNext()) {
            result *= multiplierIter.next();
        }

        /*
        final TFloatIterator postModifierIter = postModifiers.iterator();
        while (postModifierIter.hasNext()) {
            result += postModifierIter.next();
        }
        */
        return result;
    }

    public double getDurationResultValue() {
        // For now, add all modifiers and multiply by all multipliers. Negative modifiers cap to zero, but negative
        // multipliers remain.

        double result = potion.duration;
        TFloatIterator modifierIter = durationModifiers.iterator();
        while (modifierIter.hasNext()) {
            result += modifierIter.next();
        }
        result = Math.max(0, result);

        TFloatIterator multiplierIter = magnitudeMultipliers.iterator();
        while (multiplierIter.hasNext()) {
            result *= multiplierIter.next();
        }

        /*
        final TFloatIterator postModifierIter = postModifiers.iterator();
        while (postModifierIter.hasNext()) {
            result += postModifierIter.next();
        }
        */
        return result;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        consumed = true;
    }
}
