// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.herbalism.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.herbalism.component.PotionComponent;

public class DrinkPotionEvent implements Event {

    private final PotionComponent potion;
    private EntityRef instigator;
    private EntityRef item;

    public DrinkPotionEvent(PotionComponent p) {
        potion = p;
    }

    public DrinkPotionEvent(PotionComponent p, EntityRef ref) {
        potion = p;
        instigator = ref;
    }

    public DrinkPotionEvent(PotionComponent p, EntityRef instigatorRef, EntityRef itemRef) {
        potion = p;
        instigator = instigatorRef;
        item = itemRef;
    }

    public PotionComponent getPotionComponent() {
        return potion;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getItem() {
        return item;
    }
}
