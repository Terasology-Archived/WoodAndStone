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
package org.terasology.herbalism.effect;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.health.event.DoRestoreEvent;
import org.terasology.herbalism.HerbEffect;
import org.terasology.math.TeraMath;

public class HealEffect implements HerbEffect {
    private int maxHeal = 100;

    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, float magnitude, long duration) {
        entity.send(new DoRestoreEvent(TeraMath.floorToInt(maxHeal * magnitude), instigator));
    }
}
