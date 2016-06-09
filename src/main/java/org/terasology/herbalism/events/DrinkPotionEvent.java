/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.herbalism.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.herbalism.component.PotionComponent;

public class DrinkPotionEvent implements Event {

    private PotionComponent potion;
    private EntityRef instigator;
    private EntityRef item;

    public DrinkPotionEvent(PotionComponent p){
        potion = p;
    }

    public DrinkPotionEvent(PotionComponent p, EntityRef ref){
        potion = p;
        instigator = ref;
    }

    public DrinkPotionEvent(PotionComponent p, EntityRef instigatorRef, EntityRef itemRef){
        potion = p;
        instigator = instigatorRef;
        item = itemRef;
    }

    public PotionComponent getPotionComponent() {
        return potion;
    }

    public EntityRef getInstigator() { return instigator; }

    public EntityRef getItem() { return item; }
}
