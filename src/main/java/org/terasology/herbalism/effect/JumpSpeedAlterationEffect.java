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
package org.terasology.herbalism.effect;

import org.terasology.alterationEffects.AlterationEffect;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.herbalism.PotionCommonEffects;
import org.terasology.herbalism.system.DrinkPotionAuthoritySystem;
import org.terasology.logic.delay.DelayManager;

public class JumpSpeedAlterationEffect implements AlterationEffect {

    private DelayManager delayManager;

    public JumpSpeedAlterationEffect(Context context) {
        this.delayManager = context.get(DelayManager.class);
    }

    @Override
    public void applyEffect(EntityRef instigator, EntityRef entity, float magnitude, long duration) {
        boolean add = false;
        JumpSpeedComponent jumpSpeed = entity.getComponent(JumpSpeedComponent.class);

        if (jumpSpeed == null) {
            add = true;
            jumpSpeed = new JumpSpeedComponent();
        }

        jumpSpeed.multiplier = magnitude;

        if (add) {
            entity.addComponent(jumpSpeed);
        } else {
            entity.saveComponent(jumpSpeed);
        }

        delayManager.addDelayedAction(entity,
                DrinkPotionAuthoritySystem.EXPIRE_TRIGGER_PREFIX + PotionCommonEffects.JUMP_SPEED, duration);
    }
}
